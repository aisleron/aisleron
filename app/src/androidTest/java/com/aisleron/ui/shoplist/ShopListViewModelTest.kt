package com.aisleron.ui.shoplist

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ShopListViewModelTest {
    private lateinit var testData: TestDataManager
    private lateinit var shopListViewModel: ShopListViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        testData = TestDataManager()
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        val testUseCases = TestUseCaseProvider(testData)

        shopListViewModel = ShopListViewModel(
            testUseCases.getShopsUseCase,
            testUseCases.getPinnedShopsUseCase,
            testUseCases.removeLocationUseCase,
            testUseCases.getLocationUseCase,
            testScope
        )
    }

    @Test
    fun hydratePinnedShops_MethodCalled_ReturnsOnlyPinnedShops() {
        val pinnedShopCount =
            runBlocking {
                testData.locationRepository.getAll()
                    .count { it.pinned && it.type == LocationType.SHOP }
            }

        shopListViewModel.hydratePinnedShops()
        val shops =
            (shopListViewModel.shopListUiState.value as ShopListViewModel.ShopListUiState.Updated).shops

        Assert.assertEquals(pinnedShopCount, shops.count())
    }

    @Test
    fun hydrateAllShops_MethodCalled_ReturnsAllShops() {
        val shopCount = runBlocking {
            testData.locationRepository.getAll().count { it.type == LocationType.SHOP }
        }

        shopListViewModel.hydrateAllShops()
        val shops =
            (shopListViewModel.shopListUiState.value as ShopListViewModel.ShopListUiState.Updated).shops

        Assert.assertEquals(shopCount, shops.count())
    }

    @Test
    fun removeItem_ValidLocation_LocationRemoved() {
        val location = runBlocking { testData.locationRepository.getAll().first() }
        val shopListItemViewModel = ShopListItemViewModel(
            name = location.name,
            id = location.id,
            defaultFilter = location.defaultFilter
        )

        shopListViewModel.removeItem(shopListItemViewModel)
        val removedLocation = runBlocking { testData.locationRepository.get(location.id) }

        Assert.assertNull(removedLocation)
    }

    @Test
    fun removeItem_InvalidLocation_NoLocationRemoved() {
        val shopListItemViewModel = ShopListItemViewModel(
            name = "Dummy Location",
            id = -1,
            defaultFilter = FilterType.ALL
        )

        val countBefore = runBlocking { testData.locationRepository.getAll().count() }
        shopListViewModel.removeItem(shopListItemViewModel)
        val countAfter = runBlocking { testData.locationRepository.getAll().count() }

        Assert.assertEquals(countBefore, countAfter)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun removeItem_ExceptionRaised_ShopListUiStateIsError() {
        val testUseCases = TestUseCaseProvider(testData)
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        val exceptionMessage = "Error on remove location"

        val sli = ShopListViewModel(
            testUseCases.getShopsUseCase,
            testUseCases.getPinnedShopsUseCase,
            object : RemoveLocationUseCase {
                override suspend operator fun invoke(location: Location) {
                    throw Exception("Error on remove location")
                }
            },
            testUseCases.getLocationUseCase,
            testScope
        )

        val location = runBlocking { testData.locationRepository.getAll().first() }
        val shopListItemViewModel = ShopListItemViewModel(
            name = location.name,
            id = location.id,
            defaultFilter = location.defaultFilter
        )

        sli.removeItem(shopListItemViewModel)

        Assert.assertTrue(sli.shopListUiState.value is ShopListViewModel.ShopListUiState.Error)
        Assert.assertEquals(
            AisleronException.GENERIC_EXCEPTION,
            (sli.shopListUiState.value as ShopListViewModel.ShopListUiState.Error).errorCode
        )
        Assert.assertEquals(
            exceptionMessage,
            (sli.shopListUiState.value as ShopListViewModel.ShopListUiState.Error).errorMessage
        )
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ShopListViewModelReturned() {
        val testUseCases = TestUseCaseProvider(testData)
        val sli = ShopListViewModel(
            testUseCases.getShopsUseCase,
            testUseCases.getPinnedShopsUseCase,
            testUseCases.removeLocationUseCase,
            testUseCases.getLocationUseCase
        )

        Assert.assertNotNull(sli)
    }
}