package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.location.Location
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
        getPinnedShopsUseCase = GetPinnedShopsUseCase(testData.locationRepository)
    }

    @Test
    fun getPinnedShops_NoPinnedShopsDefined_ReturnEmptyList() {
        val resultList: List<Location> =
            runBlocking {
                testData.locationRepository.getAll().filter { it.pinned }
                    .forEach { testData.locationRepository.remove(it) }

                getPinnedShopsUseCase().first()
            }
        Assertions.assertEquals(0, resultList.count())
    }

    @Test
    fun getShops_PinnedShopsDefined_ReturnPinnedShopsList() {
        val pinnedCount: Int
        val resultList: List<Location> =
            runBlocking {
                pinnedCount = testData.locationRepository.getAll().count { it.pinned }
                getPinnedShopsUseCase().first()
            }

        Assertions.assertEquals(pinnedCount, resultList.count())
    }
}