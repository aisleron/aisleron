package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateLocationUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var updateLocationUseCase: UpdateLocationUseCase
    private lateinit var existingLocation: Location

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val locationRepository = testData.getRepository<LocationRepository>()

        existingLocation = runBlocking {
            locationRepository.getAll().first { it.type == LocationType.SHOP }
        }

        updateLocationUseCase = UpdateLocationUseCase(
            locationRepository,
            IsLocationNameUniqueUseCase(locationRepository)
        )
    }

    @Test
    fun updateLocation_IsDuplicateName_ThrowsException() {
        runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            val id = locationRepository.add(
                Location(
                    id = 0,
                    type = LocationType.SHOP,
                    defaultFilter = FilterType.NEEDED,
                    name = "Shop 99",
                    pinned = false,
                    aisles = emptyList()
                )
            )

            val updateLocation =
                locationRepository.get(id)!!.copy(name = existingLocation.name)
            assertThrows<AisleronException.DuplicateLocationNameException> {
                updateLocationUseCase(updateLocation)
            }
        }
    }

    @Test
    fun updateLocation_IsExistingLocation_LocationUpdated() {
        val updateLocation =
            existingLocation.copy(
                name = existingLocation.name + " Updated",
                pinned = !existingLocation.pinned
            )
        val updatedLocation: Location?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            countBefore = locationRepository.getAll().count()
            updateLocationUseCase(updateLocation)
            updatedLocation = locationRepository.getByName(updateLocation.name)
            countAfter = locationRepository.getAll().count()
        }
        assertNotNull(updatedLocation)
        assertEquals(countBefore, countAfter)
        assertEquals(updateLocation.id, updatedLocation?.id)
        assertEquals(updateLocation.name, updatedLocation?.name)
        assertEquals(updateLocation.type, updatedLocation?.type)
        assertEquals(updateLocation.pinned, updatedLocation?.pinned)
        assertEquals(updateLocation.defaultFilter, updatedLocation?.defaultFilter)
    }

    @Test
    fun updateLocation_IsNewLocation_RecordCreated() {
        val newLocation = existingLocation.copy(
            id = 0,
            name = existingLocation.name + " Inserted"
        )
        val updatedLocation: Location?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            countBefore = locationRepository.getAll().count()
            updateLocationUseCase(newLocation)
            updatedLocation = locationRepository.getByName(newLocation.name)
            countAfter = locationRepository.getAll().count()
        }
        assertNotNull(updatedLocation)
        assertEquals(countBefore + 1, countAfter)
        assertEquals(newLocation.name, updatedLocation?.name)
        assertEquals(newLocation.type, updatedLocation?.type)
        assertEquals(newLocation.pinned, updatedLocation?.pinned)
        assertEquals(newLocation.defaultFilter, updatedLocation?.defaultFilter)
    }
}