/*
 * Copyright (C) 2025 aisleron.com
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

import android.content.ContentValues
import android.database.Cursor
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals


class DatabaseMigrationTest {
    private val testDb = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AisleronDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val locationName = "Home"

        helper.createDatabase(testDb, 1).apply {
            val insertValues = ContentValues()
            insertValues.put("type", LocationType.HOME.toString())
            insertValues.put("defaultFilter", FilterType.NEEDED.toString())
            insertValues.put("name", locationName)
            insertValues.put("pinned", false)

            // Database has schema version 1. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            insert("Location", android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL, insertValues)

            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        val db = helper.runMigrationsAndValidate(testDb, 2, true)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        var showDefaultAisle: Int
        db.apply {
            val queryBuilder = SupportSQLiteQueryBuilder
                .builder("Location")
                .selection("name = ?", arrayOf(locationName))//.columns(arrayOf("showDefaultAisle"))

            val cursor: Cursor = query(queryBuilder.create())
            cursor.moveToFirst()
            showDefaultAisle = cursor.getInt(cursor.getColumnIndex("showDefaultAisle"))
            cursor.close()
            close()
        }

        assertEquals(1, showDefaultAisle)
    }
}

/**
 * ToDo: Test All migrations: https://developer.android.com/training/data-storage/room/migrating-db-versions#all-migrations-test
 */