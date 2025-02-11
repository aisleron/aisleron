package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
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
        val locationRepository = testData.getRepository<LocationRepository>()
        val aisleRepository = testData.getRepository<AisleRepository>()
        val aisleProductRepository = testData.getRepository<AisleProductRepository>()

        removeLocationUseCase = RemoveLocationUseCaseImpl(
            locationRepository,
            RemoveAisleUseCaseImpl(
                aisleRepository,
                UpdateAisleProductsUseCase(aisleProductRepository),
                RemoveProductsFromAisleUseCase(aisleProductRepository)
            ),
            RemoveDefaultAisleUseCase(
                aisleRepository, RemoveProductsFromAisleUseCase(aisleProductRepository)
            )
        )

        runBlocking {
            existingLocation = locationRepository.get(1)!!
        }
    }

    @Test
    fun removeLocation_IsExistingLocation_LocationRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedLocation: Location?
        runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            countBefore = locationRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            removedLocation = locationRepository.get(existingLocation.id)
            countAfter = locationRepository.getAll().count()
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
            val aisleRepository = testData.getRepository<AisleRepository>()
            aisleCountLocation = aisleRepository.getForLocation(existingLocation.id).count()
            aisleCountBefore = aisleRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            aisleCountAfter = aisleRepository.getAll().count()
        }
        assertEquals(aisleCountBefore - aisleCountLocation, aisleCountAfter)
    }

    @Test
    fun removeLocation_LocationRemoved_AisleProductsRemoved() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCountLocation: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            val aisles =
                testData.getRepository<AisleRepository>().getForLocation(existingLocation.id)
            aisleProductCountLocation =
                aisleProductRepository.getAll().count { it.aisleId in aisles.map { a -> a.id } }
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
        }
        assertEquals(aisleProductCountBefore - aisleProductCountLocation, aisleProductCountAfter)
    }

    @Test
    fun removeLocation_LocationHasNoAisles_LocationRemoved() {
        val removedLocation = runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            val newLocationId = locationRepository.add(
                Location(
                    id = 0,
                    type = LocationType.SHOP,
                    defaultFilter = FilterType.NEEDED,
                    name = "Dummy Shop",
                    pinned = false,
                    aisles = emptyList()
                )
            )
            val newLocation = locationRepository.get(newLocationId)!!
            removeLocationUseCase(newLocation)
            locationRepository.get(newLocation.id)
        }
        assertNull(removedLocation)
    }
}