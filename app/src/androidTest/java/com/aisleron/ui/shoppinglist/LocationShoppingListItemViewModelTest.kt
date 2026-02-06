/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.ui.shoppinglist

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationShoppingListItemViewModelTest : KoinTest {
    private val locationRepository: LocationRepository by lazy { get<LocationRepository>() }

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private suspend fun getLocation(type: LocationType): Location =
        locationRepository.getAll().first { it.type == type }

    private suspend fun getShop(): Location = getLocation(LocationType.SHOP)

    private fun getLocationShoppingListItemViewModel(existingLocation: Location): LocationShoppingListItemViewModel {
        return LocationShoppingListItemViewModel(
            location = existingLocation,
            selected = false,
            getLocationUseCase = get<GetLocationUseCase>(),
            removeLocationUseCase = get<RemoveLocationUseCase>()
        )
    }

    @Test
    fun removeItem_ItemIsValidLocation_LocationRemoved() = runTest {
        val existingLocation = getShop()
        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        shoppingListItem.remove()

        val removedLocation = locationRepository.get(existingLocation.id)
        assertNull(removedLocation)
    }

    @Test
    fun removeItem_ItemIsInvalidLocation_NoLocationRemoved() = runTest {
        val location = Location(
            rank = 1000,
            id = -1,
            name = "Dummy",
            type = LocationType.SHOP,
            expanded = false,
            aisles = emptyList(),
            defaultFilter = FilterType.NEEDED,
            pinned = false,
            showDefaultAisle = true
        )

        val shoppingListItem = getLocationShoppingListItemViewModel(location)
        val locationCountBefore = locationRepository.getAll().count()

        shoppingListItem.remove()

        val locationCountAfter = locationRepository.getAll().count()
        assertEquals(locationCountBefore, locationCountAfter)
    }

    @Test
    fun updateItemRank_LocationMoved_LocationRankUpdated() = runTest {
        val movedShop = getShop()
        val shoppingListItem = getLocationShoppingListItemViewModel(movedShop)
        val precedingLocation = locationRepository.getAll()
            .first { it.type == movedShop.type && it.id != movedShop.id }

        val precedingItem = getLocationShoppingListItemViewModel(precedingLocation)

        shoppingListItem.updateRank(precedingItem)

        val updatedLocation = locationRepository.get(movedShop.id)
        assertEquals(precedingItem.rank + 1, updatedLocation?.rank)
    }

    @Test
    fun updateItemRank_NullPrecedingItem_RankIsOne() = runTest {
        val movedLocation = getShop()
        val shoppingListItem = getLocationShoppingListItemViewModel(movedLocation)

        shoppingListItem.updateRank(null)

        val updatedLocation = locationRepository.get(movedLocation.id)
        assertEquals(1, updatedLocation?.rank)
    }

    private suspend fun updateExpandedArrangeActAssert(expanded: Boolean) {
        val location = getShop().copy(expanded = !expanded)
        locationRepository.update(location)

        val shoppingListItem = getLocationShoppingListItemViewModel(location)

        shoppingListItem.updateExpanded(expanded)

        val updatedLocation = locationRepository.get(location.id)
        assertEquals(expanded, updatedLocation?.expanded)
    }

    @Test
    fun updateExpanded_ExpandedTrue_LocationUpdatedToExpanded() = runTest {
        updateExpandedArrangeActAssert(true)
    }

    @Test
    fun updateExpanded_ExpandedFalse_LocationUpdatedToNotExpanded() = runTest {
        updateExpandedArrangeActAssert(false)
    }

    @Test
    fun onCreate_PropertiesInitializedCorrectly() = runTest {
        val existingLocation = getShop()

        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        assertEquals(existingLocation.id, shoppingListItem.id)
        assertEquals(existingLocation.name, shoppingListItem.name)
        assertEquals(existingLocation.rank, shoppingListItem.rank)
        assertEquals(existingLocation.id, shoppingListItem.locationId)
        assertEquals(false, shoppingListItem.isDefault)
        assertEquals(existingLocation.expanded, shoppingListItem.expanded)
        // Not populating aisle for these items so can't validate counts
        // assertEquals(existingLocation.aisles.first().id, shoppingListItem.aisleId)
        // assertEquals(
        //     existingLocation.aisles.sumOf { it.products.size }, shoppingListItem.childCount
        // )
    }

    @Test
    fun navigateToEditEvent_ReturnsNavigateToEditLocationEvent() = runTest {
        val existingLocation = getShop()
        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        val event = shoppingListItem.editNavigationEvent()

        assertEquals(
            ShoppingListViewModel.ShoppingListEvent.NavigateToEditLocation(existingLocation.id),
            event
        )
    }

}