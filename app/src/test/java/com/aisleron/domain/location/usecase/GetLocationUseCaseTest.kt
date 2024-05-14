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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetLocationUseCaseTest {

    private lateinit var getLocationUseCase: GetLocationUseCase

    @BeforeEach
    fun setUp() {
        getLocationUseCase = GetLocationUseCase(locationRepository)
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

    companion object {

        private lateinit var locationRepository: LocationRepositoryImpl

        @JvmStatic
        @BeforeAll
        fun beforeSpec() {
            locationRepository = LocationRepositoryImpl(LocationDaoTestImpl(), LocationMapper())

            runBlocking {
                locationRepository.add(
                    listOf(
                        Location(
                            id = 1,
                            type = LocationType.HOME,
                            defaultFilter = FilterType.NEEDED,
                            name = "Home",
                            pinned = false,
                            aisles = emptyList()
                        ), Location(
                            id = 2,
                            type = LocationType.SHOP,
                            defaultFilter = FilterType.NEEDED,
                            name = "Shop 1",
                            pinned = false,
                            aisles = emptyList()
                        ), Location(
                            id = 3,
                            type = LocationType.SHOP,
                            defaultFilter = FilterType.NEEDED,
                            name = "Shop 2",
                            pinned = false,
                            aisles = emptyList()
                        )
                    )
                )
            }
        }
    }
}