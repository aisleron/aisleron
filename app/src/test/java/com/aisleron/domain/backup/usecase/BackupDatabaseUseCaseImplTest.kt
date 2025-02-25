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

package com.aisleron.domain.backup.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.base.AisleronException
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceDbNameTestImpl
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceTestImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

class BackupDatabaseUseCaseImplTest {
    private lateinit var testData: TestDataManager

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
    }

    @Test
    fun backupDb_IsNullDbName_ThrowsInvalidDbNameException() {
        val dbMaintenance = DatabaseMaintenanceDbNameTestImpl(null)
        val backupDatabaseUseCase = BackupDatabaseUseCaseImpl(dbMaintenance)
        runBlocking {
            assertThrows<AisleronException.InvalidDbNameException> {
                backupDatabaseUseCase(URI(""))
            }
        }
    }

    @Test
    fun backupDb_IsBlankDbName_ThrowsInvalidDbNameException() {
        val dbMaintenance = DatabaseMaintenanceDbNameTestImpl("")
        val backupDatabaseUseCase = BackupDatabaseUseCaseImpl(dbMaintenance)
        runBlocking {
            assertThrows<AisleronException.InvalidDbNameException> {
                backupDatabaseUseCase(URI(""))
            }
        }
    }

    @Test
    fun backupDb_BackupFileNameCreated_HasCorrectNameFormat() {
        val dbMaintenance = DatabaseMaintenanceTestImpl()
        val srcFileName = dbMaintenance.getDatabaseName().substringBeforeLast(".")
        val srcFileExt = dbMaintenance.getDatabaseName().substringAfterLast(".")
        val backupDatabaseUseCase = BackupDatabaseUseCaseImpl(dbMaintenance)

        runBlocking {
            backupDatabaseUseCase(URI(""))
        }

        val filenameRegexTemplate = "%s-backup-(20\\d{2}[01]\\d[0-3]\\d)_([0-2]\\d([0-5]\\d){2}).%s"
        val filenameRegex = String.format(filenameRegexTemplate, srcFileName, srcFileExt)
        val regex = Regex(filenameRegex)

        assertTrue(regex.matches(dbMaintenance.backupFileName))
    }

    @Test
    fun backupDb_PassesUriToDbMaintenance() {
        val dbMaintenance = DatabaseMaintenanceTestImpl()
        val backupDatabaseUseCase = BackupDatabaseUseCaseImpl(dbMaintenance)
        val uri = URI("content://dummy.uri/for-test")

        runBlocking {
            backupDatabaseUseCase(uri)
        }

        assertEquals(uri, dbMaintenance.backupFolderUri)
    }
}
