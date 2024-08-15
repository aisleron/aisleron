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