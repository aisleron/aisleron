package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetHomeLocationUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getHomeLocationUseCase: GetHomeLocationUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getHomeLocationUseCase = GetHomeLocationUseCase(testData.locationRepository)
    }

    @Test
    fun getHomeLocation_WhenCalled_ReturnHomeLocation() {
        val location = runBlocking { getHomeLocationUseCase() }
        assertNotNull(location)
        assertEquals(LocationType.HOME, location.type)
    }
}