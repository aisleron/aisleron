package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.location.Location
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveLocationUseCaseTest {

    private lateinit var testData: TestDataManager

    private lateinit var removeLocationUseCase: RemoveLocationUseCase
    private lateinit var existingLocation: Location

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()

        removeLocationUseCase = RemoveLocationUseCase(
            testData.locationRepository,
            RemoveAisleUseCase(
                testData.aisleRepository,
                UpdateAisleProductsUseCase(testData.aisleProductRepository),
                RemoveProductsFromAisleUseCase(testData.aisleProductRepository)
            ),
            RemoveDefaultAisleUseCase(
                testData.aisleRepository,
                RemoveProductsFromAisleUseCase(testData.aisleProductRepository)
            )
        )

        runBlocking {
            existingLocation = testData.locationRepository.get(1)!!
        }
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun removeLocation_IsExistingLocation_LocationRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedLocation: Location?
        runBlocking {
            countBefore = testData.locationRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            removedLocation = testData.locationRepository.get(existingLocation.id)
            countAfter = testData.locationRepository.getAll().count()
        }
        assertNull(removedLocation)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeLocation_LocationRemoved_AislesRemoved() {
        val aisleCountBefore: Int
        val aisleCountAfter: Int
        val aisleCountLocation: Int
        runBlocking {
            aisleCountLocation = testData.aisleRepository.getForLocation(existingLocation.id).count()
            aisleCountBefore = testData.aisleRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            aisleCountAfter = testData.aisleRepository.getAll().count()
        }
        assertEquals(aisleCountBefore - aisleCountLocation, aisleCountAfter)
    }

    @Test
    fun removeLocation_LocationRemoved_AisleProductsRemoved() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCountLocation: Int
        runBlocking {
            val aisles = testData.aisleRepository.getForLocation(existingLocation.id)
            aisleProductCountLocation =
                testData.aisleProductRepository.getAll().count { it.aisleId in aisles.map { a -> a.id } }
            aisleProductCountBefore = testData.aisleProductRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            aisleProductCountAfter = testData.aisleProductRepository.getAll().count()
        }
        assertEquals(aisleProductCountBefore - aisleProductCountLocation, aisleProductCountAfter)
    }
}