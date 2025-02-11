package com.aisleron.domain.location.usecase

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.data.TestDataManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPinnedShopsUseCaseTest {

    private lateinit var testData: TestDataManager

    private lateinit var getPinnedShopsUseCase: GetPinnedShopsUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getPinnedShopsUseCase = GetPinnedShopsUseCase(testData.getRepository<LocationRepository>())
    }

    @Test
    fun getPinnedShops_NoPinnedShopsDefined_ReturnEmptyList() {
        val resultList: List<Location> =
            runBlocking {
                val locationRepository = testData.getRepository<LocationRepository>()
                locationRepository.getAll().filter { it.pinned }
                    .forEach { locationRepository.remove(it) }

                getPinnedShopsUseCase().first()
            }
        Assertions.assertEquals(0, resultList.count())
    }

    @Test
    fun getShops_PinnedShopsDefined_ReturnPinnedShopsList() {
        val pinnedCount: Int
        val resultList: List<Location> =
            runBlocking {
                pinnedCount =
                    testData.getRepository<LocationRepository>().getAll().count { it.pinned }
                getPinnedShopsUseCase().first()
            }

        Assertions.assertEquals(pinnedCount, resultList.count())
    }
}