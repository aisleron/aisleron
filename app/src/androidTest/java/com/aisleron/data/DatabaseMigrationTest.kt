/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data

import android.database.Cursor
import androidx.core.content.contentValuesOf
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.data.base.SyncEntity
import com.aisleron.data.migration.Migration6To7
import com.aisleron.data.migration.Migration8To9
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DatabaseMigrationTest {
    private val testDb = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AisleronDatabase::class.java
    )

    private fun populateDatabase(db: SupportSQLiteDatabase, version: Int) {
        // You can't use DAO classes because they expect the latest schema.

        val locationId = db.insert(
            "Location",
            android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
            contentValuesOf(
                "type" to LocationType.HOME.toString(),
                "defaultFilter" to FilterType.NEEDED.toString(),
                "name" to "Home",
                "pinned" to false
            ).apply {
                if (version >= 7) put("rank", 1)
            }
        )

        val aisleId = db.insert(
            "Aisle",
            android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
            contentValuesOf(
                "name" to "No Aisle",
                "locationId" to locationId,
                "rank" to 1,
                "isDefault" to true
            ).apply {}
        )

        val productId = db.insert(
            "Product",
            android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
            contentValuesOf(
                "name" to "Migration Test Product",
                "inStock" to true
            ).apply {
                if (version >= 5) put("qtyNeeded", 10)
            }
        )

        db.insert(
            "AisleProduct",
            android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
            contentValuesOf(
                "aisleId" to aisleId,
                "productId" to productId,
                "rank" to 100
            ).apply {}
        )

        if (version >= 3) {
            val loyaltyCardId = db.insert(
                "LoyaltyCard",
                android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
                contentValuesOf(
                    "name" to "A Loyalty Card for testing",
                    "provider" to LoyaltyCardProviderType.CATIMA.name,
                    "intent" to "testIntent"
                ).apply {}
            )

            db.insert(
                "LocationLoyaltyCard",
                android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
                contentValuesOf(
                    "locationId" to locationId,
                    "loyaltyCardId" to loyaltyCardId

                ).apply {}
            )
        }

        if (version >= 5) {
            db.insert(
                "Note",
                android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
                contentValuesOf(
                    "noteText" to "A note for testing"
                ).apply {}
            )
        }

        if (version >= 8) {
            db.insert(
                "ProductVariant",
                android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL,
                contentValuesOf(
                    "productId" to productId,
                    "barcode" to "123456789"
                ).apply {}
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate1to2() {
        helper.createDatabase(testDb, 1).apply {
            populateDatabase(this, 1)
            close()
        }

        // Re-open the database with version 2
        val db = helper.runMigrationsAndValidate(testDb, 2, true)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        var showDefaultAisle: Int
        db.apply {
            val queryBuilder = SupportSQLiteQueryBuilder.builder("Location")
            val cursor: Cursor = query(queryBuilder.create())
            cursor.moveToFirst()
            showDefaultAisle = cursor.getInt(cursor.getColumnIndex("showDefaultAisle"))
            cursor.close()
            close()
        }

        assertEquals(1, showDefaultAisle)
    }

    @Test
    @Throws(IOException::class)
    fun migrate2to3() {
        helper.createDatabase(testDb, 2).apply {
            populateDatabase(this, 2)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 3, true)

        db.apply {
            val queryBuilder = SupportSQLiteQueryBuilder.builder("LoyaltyCard")
            val cursor: Cursor = query(queryBuilder.create())
            assertEquals(0, cursor.count)
            cursor.close()
            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate3to4() {
        helper.createDatabase(testDb, 3).apply {
            populateDatabase(this, 3)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 4, true)
        var qtyNeeded = -1

        db.apply {
            val queryBuilder = SupportSQLiteQueryBuilder.builder("Product")
            val cursor: Cursor = query(queryBuilder.create())
            cursor.moveToFirst()
            qtyNeeded = cursor.getInt(cursor.getColumnIndex("qtyNeeded"))
            cursor.close()
            close()
        }
        assertEquals(0, qtyNeeded)
    }

    @Test
    @Throws(IOException::class)
    fun migrate4to5() {
        helper.createDatabase(testDb, 4).apply {
            populateDatabase(this, 4)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 5, true)

        db.apply {
            // Check noteId exists on Product
            val queryProduct = SupportSQLiteQueryBuilder.builder("Product")
            val cursorProduct: Cursor = query(queryProduct.create())
            cursorProduct.moveToFirst()
            val noteId = cursorProduct.getIntOrNull(cursorProduct.getColumnIndex("noteId"))
            cursorProduct.close()

            assertNull(noteId)

            // Check Note table exists
            val queryNote = SupportSQLiteQueryBuilder.builder("Note")
            val cursorNote: Cursor = query(queryNote.create())
            assertEquals(0, cursorNote.count)
            cursorNote.close()

            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate5to6() {
        helper.createDatabase(testDb, 5).apply {
            populateDatabase(this, 5)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 6, true)

        db.apply {
            // Check noteId exists on Product
            val queryProduct = SupportSQLiteQueryBuilder.builder("Product")
            val cursorProduct: Cursor = query(queryProduct.create())
            cursorProduct.moveToFirst()

            val qtyNeeded = cursorProduct.getDouble(cursorProduct.getColumnIndex("qtyNeeded"))
            assertNotNull(qtyNeeded)

            val qtyIncrement = cursorProduct.getDouble(cursorProduct.getColumnIndex("qtyIncrement"))
            assertEquals(1.0, qtyIncrement)

            val unitOfMeasure =
                cursorProduct.getString(cursorProduct.getColumnIndex("unitOfMeasure"))

            assertEquals("", unitOfMeasure)

            val trackingMode = cursorProduct.getString(cursorProduct.getColumnIndex("trackingMode"))
            assertNull(trackingMode)

            cursorProduct.close()

            close()
        }
    }


    @Test
    @Throws(IOException::class)
    fun migrate6to7() {
        helper.createDatabase(testDb, 6).use { db ->
            populateDatabase(db, 6)
        }

        helper.runMigrationsAndValidate(
            testDb, 7, true, Migration6To7()
        ).use { db ->
            val queryLocation = SupportSQLiteQueryBuilder.builder("Location")
            val cursorLocation: Cursor = db.query(queryLocation.create())
            cursorLocation.moveToFirst()

            // Check expanded exists on Location
            val expanded = cursorLocation.getInt(cursorLocation.getColumnIndex("expanded"))
            assertEquals(1, expanded)

            // Check migration sets initial rank equal to id
            val id = cursorLocation.getInt(cursorLocation.getColumnIndex("id"))
            val rank = cursorLocation.getInt(cursorLocation.getColumnIndex("rank"))
            assertEquals(id, rank)

            cursorLocation.close()
        }
    }

    private fun validateV8SyncColumns(
        tableName: String, db: SupportSQLiteDatabase, validateDefaults: Boolean
    ) {
        val querySyncEntity = SupportSQLiteQueryBuilder.builder(tableName)
        val cursor: Cursor = db.query(querySyncEntity.create())

        assertNotEquals(-1, cursor.getColumnIndex("syncId"))
        assertNotEquals(-1, cursor.getColumnIndex("isRemoved"))
        assertNotEquals(-1, cursor.getColumnIndex("lastModifiedAt"))
        assertNotEquals(-1, cursor.getColumnIndex("serverUpdatedAt"))

        if (validateDefaults) {
            cursor.moveToFirst()

            val syncId = cursor.getStringOrNull(cursor.getColumnIndex("syncId"))
            assertNull(syncId)

            val isRemoved = cursor.getInt(cursor.getColumnIndex("isRemoved"))
            assertEquals(0, isRemoved)

            val lastModifiedAt = cursor.getLong(cursor.getColumnIndex("lastModifiedAt"))
            assertEquals(0, lastModifiedAt)

            val serverUpdatedAt = cursor.getLongOrNull(cursor.getColumnIndex("syncId"))
            assertNull(serverUpdatedAt)
        }

        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate7to8() {
        helper.createDatabase(testDb, 7).apply {
            populateDatabase(this, 7)
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 8, true)

        db.apply {
            // Check ProductVariant table exists and has correct schema
            val queryVariants = SupportSQLiteQueryBuilder.builder("ProductVariant")
            val cursorVariants: Cursor = query(queryVariants.create())
            assertEquals(0, cursorVariants.count)
            cursorVariants.close()

            validateV8SyncColumns("Aisle", this, true)
            validateV8SyncColumns("AisleProduct", this, false)
            validateV8SyncColumns("Location", this, true)
            validateV8SyncColumns("LoyaltyCard", this, false)
            validateV8SyncColumns("Note", this, false)
            validateV8SyncColumns("Product", this, true)
            validateV8SyncColumns("ProductVariant", this, false)

            close()
        }
    }

    private fun assertAisleProductForeignKeysExistV9(db: SupportSQLiteDatabase) {
        val foreignKeys = mutableListOf<String>()

        db.query("PRAGMA foreign_key_list('AisleProduct')").use { cursor ->
            val tableIndex = cursor.getColumnIndex("table")
            val fromIndex = cursor.getColumnIndex("from")

            while (cursor.moveToNext()) {
                val parentTable = cursor.getString(tableIndex)
                val childColumn = cursor.getString(fromIndex)
                foreignKeys.add("$childColumn -> $parentTable")
            }
        }

        // Assert both foreign keys exist
        assertTrue(foreignKeys.contains("aisleId -> Aisle"))
        assertTrue(foreignKeys.contains("productId -> Product"))
    }

    @Test
    @Throws(IOException::class)
    fun migrate8to9() {
        helper.createDatabase(testDb, 8).use { db ->
            populateDatabase(db, 8)

            db.execSQL(
                """
                INSERT INTO AisleProduct (aisleId, productId, rank) VALUES
                (1, 999, 100),
                (999, 1, 100),
                (999, 999, 100)
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(
            testDb, 9, true, Migration8To9()
        ).use { db ->
            val count = db.compileStatement(
                "SELECT COUNT(*) FROM AisleProduct WHERE aisleId = 999 or productId =999"
            ).simpleQueryForLong()

            assertEquals(0L, count)

            assertAisleProductForeignKeysExistV9(db)

            val queryNote = SupportSQLiteQueryBuilder.builder("Note")
            db.query(queryNote.create()).use { cursor ->
                cursor.moveToFirst()

                // Check createdAt exists on Note
                val expanded = cursor.getInt(cursor.getColumnIndex("createdAt"))
                assertEquals(1, expanded)

                // Check migration sets initial rank equal to id
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val createdAt = cursor.getLong(cursor.getColumnIndex("createdAt"))
                assertTrue(createdAt > 0)
                assertEquals(id.toLong(), createdAt)
            }
        }
    }

    private fun validateV8SyncEntity(entity: SyncEntity) {
        assertNull(entity.syncId)
        assertFalse(entity.isRemoved)
        assertEquals(0, entity.lastModifiedAt)
        assertNull(entity.serverUpdatedAt)
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() = runTest {
        helper.createDatabase(testDb, 1).use { db ->
            populateDatabase(db, 1)
        }

        val db = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AisleronDatabase::class.java,
            testDb
        )
            .addMigrations(Migration6To7(), Migration8To9())
            .build()

        // LoyaltyCard introduced in V3
        val loyaltyCards = db.loyaltyCardDao().getLoyaltyCards()
        assertNotNull(loyaltyCards)

        val product = db.productDao().getProducts().first()

        // Product.qtyNeeded introduced in V4, updated to Double in V6
        assertEquals(0.0, product.qtyNeeded)

        // Product.noteId introduced in V5
        assertNull(product.noteId)

        // Note introduced in V5
        val notes = db.noteDao().getNotes()
        assertNotNull(notes)

        // Product.qtyIncrement introduced in V6
        assertEquals(1.0, product.qtyIncrement)

        // Location Expanded and Rank introduced in V7
        val location = db.locationDao().getLocations().first()
        assertEquals(true, location.expanded)
        assertEquals(location.id, location.rank)

        // ProductVariant introduced in V8
        val variants = db.productVariantDao().getByProductId(product.id)
        assertNotNull(variants)

        // Sync Entity fields introduced in V8
        validateV8SyncEntity(db.aisleDao().getAisles().first())
        validateV8SyncEntity(db.locationDao().getLocations().first())
        validateV8SyncEntity(db.productDao().getProducts().first())

        db.close()
    }
}