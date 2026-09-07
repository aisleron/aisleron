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
        helper.createDatabase(testDb, 1).use { db -> populateDatabase(db, 1) }

        helper.runMigrationsAndValidate(testDb, 2, true).use { db ->
            val queryBuilder = SupportSQLiteQueryBuilder.builder("Location")
            val showDefaultAisle = db.query(queryBuilder.create()).use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(cursor.getColumnIndex("showDefaultAisle"))
            }

            assertEquals(1, showDefaultAisle)
        }

    }

    @Test
    @Throws(IOException::class)
    fun migrate2to3() {
        helper.createDatabase(testDb, 2).use { db -> populateDatabase(db, 2) }

        helper.runMigrationsAndValidate(testDb, 3, true).use { db ->
            val queryBuilder = SupportSQLiteQueryBuilder.builder("LoyaltyCard")
            db.query(queryBuilder.create()).use { cursor ->
                assertEquals(0, cursor.count)
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate3to4() {
        helper.createDatabase(testDb, 3).use { db -> populateDatabase(db, 3) }

        helper.runMigrationsAndValidate(testDb, 4, true).use { db ->
            val queryBuilder = SupportSQLiteQueryBuilder.builder("Product")
            val qtyNeeded = db.query(queryBuilder.create()).use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(cursor.getColumnIndex("qtyNeeded"))
            }

            assertEquals(0, qtyNeeded)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate4to5() {
        helper.createDatabase(testDb, 4).use { db -> populateDatabase(db, 4) }

        helper.runMigrationsAndValidate(testDb, 5, true).use { db ->
            // Check noteId exists on Product
            val queryProduct = SupportSQLiteQueryBuilder.builder("Product")
            val noteId = db.query(queryProduct.create()).use { cursor ->
                cursor.moveToFirst()
                cursor.getIntOrNull(cursor.getColumnIndex("noteId"))
            }

            assertNull(noteId)

            // Check Note table exists
            val queryNote = SupportSQLiteQueryBuilder.builder("Note")
            db.query(queryNote.create()).use { cursor ->
                assertEquals(0, cursor.count)
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate5to6() {
        helper.createDatabase(testDb, 5).use { db -> populateDatabase(db, 5) }

        helper.runMigrationsAndValidate(testDb, 6, true).use { db ->
            // Check noteId exists on Product
            val queryProduct = SupportSQLiteQueryBuilder.builder("Product")
            db.query(queryProduct.create()).use { cursor ->
                cursor.moveToFirst()

                val qtyNeeded = cursor.getDouble(cursor.getColumnIndex("qtyNeeded"))
                assertNotNull(qtyNeeded)

                val qtyIncrement = cursor.getDouble(cursor.getColumnIndex("qtyIncrement"))
                assertEquals(1.0, qtyIncrement)

                val unitOfMeasure = cursor.getString(cursor.getColumnIndex("unitOfMeasure"))
                assertEquals("", unitOfMeasure)

                val trackingMode = cursor.getString(cursor.getColumnIndex("trackingMode"))
                assertNull(trackingMode)

            }
        }
    }


    @Test
    @Throws(IOException::class)
    fun migrate6to7() {
        helper.createDatabase(testDb, 6).use { db -> populateDatabase(db, 6) }

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
        helper.createDatabase(testDb, 7).use { db -> populateDatabase(db, 7) }

        helper.runMigrationsAndValidate(testDb, 8, true).use { db ->
            // Check ProductVariant table exists and has correct schema
            val queryVariants = SupportSQLiteQueryBuilder.builder("ProductVariant")
            val cursorVariants: Cursor = db.query(queryVariants.create())
            assertEquals(0, cursorVariants.count)
            cursorVariants.close()

            validateV8SyncColumns("Aisle", db, true)
            validateV8SyncColumns("AisleProduct", db, false)
            validateV8SyncColumns("Location", db, true)
            validateV8SyncColumns("LoyaltyCard", db, false)
            validateV8SyncColumns("Note", db, false)
            validateV8SyncColumns("Product", db, true)
            validateV8SyncColumns("ProductVariant", db, false)
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate8to9_AisleProductMigrated_AisleProductForeignKeysCreated() {
        helper.createDatabase(testDb, 8).use { db -> populateDatabase(db, 8) }

        helper.runMigrationsAndValidate(
            testDb, 9, true, Migration8To9()
        ).use { db ->
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
    }

    @Test
    @Throws(IOException::class)
    fun migrate8to9_AisleProductMigrated_OrphanRecordsRemoved() {
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
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate8to9_NoteMigrated_CreatedAtAdded() {
        helper.createDatabase(testDb, 8).use { db -> populateDatabase(db, 8) }

        helper.runMigrationsAndValidate(
            testDb, 9, true, Migration8To9()
        ).use { db ->
            val queryNote = SupportSQLiteQueryBuilder.builder("Note")
            db.query(queryNote.create()).use { cursor ->
                cursor.moveToFirst()

                // Check createdAt exists on Note
                val expanded = cursor.getInt(cursor.getColumnIndex("createdAt"))
                assertEquals(1, expanded)

                // Check migration sets initial create date equal to id
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
        helper.createDatabase(testDb, 1).use { db -> populateDatabase(db, 1) }

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