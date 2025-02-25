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
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceTestImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

class RestoreDatabaseUseCaseImplTest {
    private lateinit var testData: TestDataManager

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
    }

    @Test
    fun restoreDb_PassesUriToDbMaintenance() {
        val dbMaintenance = DatabaseMaintenanceTestImpl()
        val restoreDatabaseUseCase = RestoreDatabaseUseCaseImpl(dbMaintenance)
        val uri = URI("content://dummy.uri/for-test")

        runBlocking {
            restoreDatabaseUseCase(uri)
        }

        assertEquals(uri, dbMaintenance.restoreFileUri)
    }

    @Test
    fun restoreDb_IsBlankUri_ThrowsInvalidDbRestoreFileException() {
        val dbMaintenance = DatabaseMaintenanceTestImpl()
        val restoreDatabaseUseCase = RestoreDatabaseUseCaseImpl(dbMaintenance)
        runBlocking {
            assertThrows<AisleronException.InvalidDbRestoreFileException> {
                restoreDatabaseUseCase(URI(""))
            }
        }
    }
}