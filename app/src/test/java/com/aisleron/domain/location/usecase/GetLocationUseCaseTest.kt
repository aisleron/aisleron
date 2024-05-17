package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetLocationUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getLocationUseCase: GetLocationUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getLocationUseCase = GetLocationUseCase(testData.locationRepository)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getLocation_NonExistentId_ReturnNull() {
        assertNull(runBlocking { getLocationUseCase(2001) })
    }

    @Test
    fun getLocation_ExistingId_ReturnLocation() {
        val location = runBlocking { getLocationUseCase(1) }
        assertNotNull(location)
        assertEquals(1, location!!.id)
    }
}