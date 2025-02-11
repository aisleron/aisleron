package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetShopsUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getShopsUseCase: GetShopsUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager(addData = false)
        getShopsUseCase = GetShopsUseCase(testData.getRepository<LocationRepository>())

    }

    @Test
    fun getShops_NoShopsDefined_ReturnEmptyList() {
        val resultList: List<Location> =
            runBlocking {
                getShopsUseCase().first()
            }
        assertEquals(0, resultList.count())
    }

    @Test
    fun getShops_ShopsDefined_ReturnShopsList() {
        val resultList: List<Location> =
            runBlocking {
                testData.getRepository<LocationRepository>().add(
                    listOf(
                        Location(
                            id = 1000,
                            type = LocationType.SHOP,
                            defaultFilter = FilterType.NEEDED,
                            name = "Shop 1",
                            pinned = false,
                            aisles = emptyList()
                        ),
                        Location(
                            id = 2000,
                            type = LocationType.SHOP,
                            defaultFilter = FilterType.NEEDED,
                            name = "Shop 2",
                            pinned = false,
                            aisles = emptyList()
                        ),
                    )
                )
                getShopsUseCase().first()
            }

        assertEquals(2, resultList.count())
    }
}