package com.aisleron.domain.location.usecase

import com.aisleron.data.location.LocationDaoTestImpl
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPinnedShopsUseCaseTest {
    private lateinit var getPinnedShopsUseCase: GetPinnedShopsUseCase
    private lateinit var locationRepository: LocationRepositoryImpl

    @BeforeEach
    fun setUp() {
        locationRepository = LocationRepositoryImpl(
            LocationDaoTestImpl(), LocationMapper()
        )

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
                    ),
                    Location(
                        id = 2,
                        type = LocationType.SHOP,
                        defaultFilter = FilterType.NEEDED,
                        name = "Shop 1",
                        pinned = false,
                        aisles = emptyList()
                    ),
                    Location(
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

        getPinnedShopsUseCase = GetPinnedShopsUseCase(locationRepository)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getPinnedShops_NoPinnedShopsDefined_ReturnEmptyList() {
        val resultList: List<Location> =
            runBlocking {
                getPinnedShopsUseCase().first()
            }
        Assertions.assertEquals(0, resultList.count())
    }

    @Test
    fun getShops_PinnedShopsDefined_ReturnPinnedShopsList() {
        val resultList: List<Location> =
            runBlocking {
                locationRepository.add(
                    listOf(
                        Location(
                            id = 4,
                            type = LocationType.SHOP,
                            defaultFilter = FilterType.NEEDED,
                            name = "Shop 3",
                            pinned = true,
                            aisles = emptyList()
                        ),
                        Location(
                            id = 5,
                            type = LocationType.SHOP,
                            defaultFilter = FilterType.NEEDED,
                            name = "Shop 4",
                            pinned = true,
                            aisles = emptyList()
                        ),
                    )
                )
                getPinnedShopsUseCase().first()
            }

        Assertions.assertEquals(2, resultList.count())
    }
}