package com.aisleron.domain.location.usecase

import com.aisleron.data.location.LocationDaoTestImpl
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsLocationNameUniqueUseCaseTest {

    private lateinit var isLocationNameUniqueUseCase: IsLocationNameUniqueUseCase

    @BeforeEach
    fun setUp() {
        isLocationNameUniqueUseCase = IsLocationNameUniqueUseCase(locationRepository)
    }

    @AfterEach
    fun tearDown() {

    }

    @Test
    fun isNameUnique_NoMatchingNameExists_ReturnTrue() {
        val newLocation = existingLocation.copy(id = 0, name = "Shop 2")
        val result = runBlocking {
            isLocationNameUniqueUseCase(newLocation)
        }
        assertNotEquals(existingLocation.name, newLocation.name)
        assertTrue(result)
    }

    @Test
    fun isNameUnique_LocationIdsMatch_ReturnTrue() {
        val newLocation = existingLocation.copy(pinned = true)
        val result = runBlocking {
            isLocationNameUniqueUseCase(newLocation)
        }
        assertEquals(existingLocation.id, newLocation.id)
        assertTrue(result)
    }

    @Test
    fun isNameUnique_LocationTypesDiffer_ReturnTrue() {
        val newLocation = existingLocation.copy(id = 0, type = LocationType.HOME)
        val result = runBlocking {
            isLocationNameUniqueUseCase(newLocation)
        }
        assertNotEquals(existingLocation.type, newLocation.type)
        assertTrue(result)
    }

    @Test
    fun isNameUnique_NamesMatchTypesMatchIdsDiffer_ReturnFalse() {
        val newLocation = existingLocation.copy(id = 0)
        val result = runBlocking {
            isLocationNameUniqueUseCase(newLocation)
        }
        assertEquals(existingLocation.name, newLocation.name)
        assertEquals(existingLocation.type, newLocation.type)
        assertNotEquals(existingLocation.id, newLocation.id)
        assertFalse(result)
    }

    companion object {

        private lateinit var locationRepository: LocationRepositoryImpl
        private lateinit var existingLocation: Location

        @JvmStatic
        @BeforeAll
        fun beforeSpec() {
            locationRepository = LocationRepositoryImpl(LocationDaoTestImpl(), LocationMapper())

            existingLocation = runBlocking {
                val id = locationRepository.add(
                    Location(
                        id = 1,
                        type = LocationType.SHOP,
                        defaultFilter = FilterType.NEEDED,
                        name = "Shop 1",
                        pinned = false,
                        aisles = emptyList()
                    )
                )
                locationRepository.get(id)!!
            }
        }
    }
}