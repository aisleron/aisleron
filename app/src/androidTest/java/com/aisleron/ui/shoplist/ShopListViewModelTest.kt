/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.ui.shoplist

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.GetPinnedShopsUseCase
import com.aisleron.domain.location.usecase.GetShopsUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertTrue

class ShopListViewModelTest : KoinTest {
    private lateinit var shopListViewModel: ShopListViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        shopListViewModel = get<ShopListViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun hydratePinnedShops_MethodCalled_ReturnsOnlyPinnedShops() = runTest {
        val pinnedShopCount =
            get<LocationRepository>().getAll().count { it.pinned && it.type == LocationType.SHOP }

        shopListViewModel.hydratePinnedShops()
        val shops =
            (shopListViewModel.shopListUiState.value as ShopListViewModel.ShopListUiState.Updated).shops

        assertTrue { pinnedShopCount > 0 }
        Assert.assertEquals(pinnedShopCount, shops.count())
    }

    @Test
    fun hydrateAllShops_MethodCalled_ReturnsAllShops() = runTest {
        val shopCount = get<LocationRepository>().getAll().count { it.type == LocationType.SHOP }

        shopListViewModel.hydrateAllShops()
        val shops =
            (shopListViewModel.shopListUiState.value as ShopListViewModel.ShopListUiState.Updated).shops

        Assert.assertEquals(shopCount, shops.count())
    }

    @Test
    fun removeItem_ValidLocation_LocationRemoved() = runTest {
        val locationRepository = get<LocationRepository>()
        val location = locationRepository.getAll().first()
        val shopListItemViewModel = ShopListItemViewModel(
            name = location.name,
            id = location.id,
            defaultFilter = location.defaultFilter
        )

        shopListViewModel.removeItem(shopListItemViewModel)
        val removedLocation = locationRepository.get(location.id)

        Assert.assertNull(removedLocation)
    }

    @Test
    fun removeItem_InvalidLocation_NoLocationRemoved() = runTest {
        val shopListItemViewModel = ShopListItemViewModel(
            name = "Dummy Location",
            id = -1,
            defaultFilter = FilterType.ALL
        )

        val locationRepository = get<LocationRepository>()
        val countBefore = locationRepository.getAll().count()
        shopListViewModel.removeItem(shopListItemViewModel)
        val countAfter = locationRepository.getAll().count()

        Assert.assertEquals(countBefore, countAfter)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun removeItem_ExceptionRaised_ShopListUiStateIsError() = runTest {
        val exceptionMessage = "Error on remove location"

        val sli = ShopListViewModel(
            getShopsUseCase = get<GetShopsUseCase>(),
            getPinnedShopsUseCase = get<GetPinnedShopsUseCase>(),
            removeLocationUseCase = object : RemoveLocationUseCase {
                override suspend operator fun invoke(location: Location) {
                    throw Exception("Error on remove location")
                }
            },
            getLocationUseCase = get<GetLocationUseCase>(),
            coroutineScopeProvider = TestScope(UnconfinedTestDispatcher())
        )

        val location = get<LocationRepository>().getAll().first()
        val shopListItemViewModel = ShopListItemViewModel(
            name = location.name,
            id = location.id,
            defaultFilter = location.defaultFilter
        )

        sli.removeItem(shopListItemViewModel)

        Assert.assertTrue(sli.shopListUiState.value is ShopListViewModel.ShopListUiState.Error)
        Assert.assertEquals(
            AisleronException.ExceptionCode.GENERIC_EXCEPTION,
            (sli.shopListUiState.value as ShopListViewModel.ShopListUiState.Error).errorCode
        )
        Assert.assertEquals(
            exceptionMessage,
            (sli.shopListUiState.value as ShopListViewModel.ShopListUiState.Error).errorMessage
        )
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ShopListViewModelReturned() {
        val sli = ShopListViewModel(
            getShopsUseCase = get<GetShopsUseCase>(),
            getPinnedShopsUseCase = get<GetPinnedShopsUseCase>(),
            removeLocationUseCase = get<RemoveLocationUseCase>(),
            getLocationUseCase = get<GetLocationUseCase>()
        )

        Assert.assertNotNull(sli)
    }
}