package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
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

    private lateinit var testData: TestDataManager

    private lateinit var getPinnedShopsUseCase: GetPinnedShopsUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getPinnedShopsUseCase = GetPinnedShopsUseCase(testData.locationRepository)
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
                testData.locationRepository.add(
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