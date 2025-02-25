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

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.data.maintenance.DatabaseMaintenanceImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before

class DatabaseMaintenanceTest {
    private lateinit var db: AisleronDatabase
    private lateinit var dbMaintenance: DatabaseMaintenanceImpl


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AisleronDatabase::class.java
        ).build()

        val testDispatcher = UnconfinedTestDispatcher()

        dbMaintenance = DatabaseMaintenanceImpl(
            db,
            InstrumentationRegistry.getInstrumentation().context,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    /*    @Test
        fun getDatabaseName() {
        }

        @Test
        fun backupDatabase_OutputStreamIsEmpty_ThrowsInvalidDbBackupFileException() {
            val uri =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI()

            Assert.assertThrows(AisleronException.InvalidDbBackupFileException::class.java) {
                runBlocking {
                    dbMaintenance.backupDatabase(URI(uri.toString()), "")
                }
            }
        }

        @Test
        fun restoreDatabase() {
        }*/
}