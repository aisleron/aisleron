package com.aisleron.domain.backup.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceTestImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
}