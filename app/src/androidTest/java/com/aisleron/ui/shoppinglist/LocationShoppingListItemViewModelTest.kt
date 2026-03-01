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
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationExpandedUseCase
import com.aisleron.domain.location.usecase.UpdateLocationRankUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.copyentity.CopyEntityType
import com.aisleron.ui.note.NoteParentRef
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

    private suspend fun getLocationShoppingListItemViewModel(existingLocation: Location): LocationShoppingListItemViewModel {
        return LocationShoppingListItemViewModel(
            selected = false,
            childCount = existingLocation.aisles.sumOf { it.products.size },
            expanded = existingLocation.expanded,
            rank = existingLocation.rank,
            id = existingLocation.id,
            name = existingLocation.name,
            aisleId = existingLocation.aisles.firstOrNull { !it.isDefault }?.id ?: 0,
            showLoyaltyCard = get<GetLoyaltyCardForLocationUseCase>().invoke(existingLocation.id) != null
        ).apply {
            removeLocationUseCase = get<RemoveLocationUseCase>()
            updateLocationRankUseCase = get<UpdateLocationRankUseCase>()
            updateLocationExpandedUseCase = get<UpdateLocationExpandedUseCase>()
            getLoyaltyCardForLocationUseCase = get<GetLoyaltyCardForLocationUseCase>()
        }
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

        val precedingId = locationRepository.add(movedShop.copy(id = 0, rank = movedShop.rank + 1))
        val precedingLocation = locationRepository.get(precedingId)!!
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
    fun navigateToEditEvent_ReturnsNavigateToEditLocationEvent() = runTest {
        val existingLocation = getShop()
        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        val event = shoppingListItem.editNavigationEvent()

        assertEquals(
            ShoppingListViewModel.ShoppingListEvent.NavigateToEditLocation(existingLocation.id),
            event
        )
    }

    @Test
    fun copyDialogNavigationEvent_ReturnsNavigateToCopyDialogEvent() = runTest {
        val existingLocation = getShop()
        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        val event = shoppingListItem.copyDialogNavigationEvent()

        val expected = ShoppingListViewModel.ShoppingListEvent.NavigateToCopyDialog(
            CopyEntityType.Location(existingLocation.id), existingLocation.name
        )

        assertEquals(expected, event)
    }

    @Test
    fun noteDialogNavigationEvent_ReturnsNavigateToNoteDialogEvent() = runTest {
        val existingLocation = getShop()
        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        val event = shoppingListItem.noteDialogNavigationEvent()

        val expected = ShoppingListViewModel.ShoppingListEvent.NavigateToNoteDialog(
            NoteParentRef.Location(existingLocation.id)
        )

        assertEquals(expected, event)
    }

    @Test
    fun navigateToLoyaltyCardEvent_LocationHasLoyaltyCard_ReturnsNavigateToLoyaltyCard() = runTest {
        val existingLocation = getShop()
        val loyaltyCardRepository = get<LoyaltyCardRepository>()
        val loyaltyCardId = loyaltyCardRepository.add(
            LoyaltyCard(
                id = 0,
                name = "Test Loyalty Card",
                provider = LoyaltyCardProviderType.CATIMA,
                intent = "Dummy Intent"
            )
        )

        loyaltyCardRepository.addToLocation(existingLocation.id, loyaltyCardId)
        val loyaltyCard = loyaltyCardRepository.get(loyaltyCardId)
        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        val event = shoppingListItem.navigateToLoyaltyCardEvent()

        val expected = ShoppingListViewModel.ShoppingListEvent.NavigateToLoyaltyCard(
            loyaltyCard
        )

        assertEquals(expected, event)
    }

    @Test
    fun navigateToLoyaltyCardEvent_NoLoyaltyCard_ReturnsEventWithNullLoyaltyCard() = runTest {
        val existingLocation = getShop()
        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        val event = shoppingListItem.navigateToLoyaltyCardEvent()

        val expected = ShoppingListViewModel.ShoppingListEvent.NavigateToLoyaltyCard(
            null
        )

        assertEquals(expected, event)
    }

    @Test
    fun uniqueId_MatchesLocationId() = runTest {
        val existingLocation = getShop()
        val expected = ShoppingListItem.UniqueId(
            ShoppingListItem.ItemType.HEADER, existingLocation.id
        )

        val shoppingListItem = getLocationShoppingListItemViewModel(existingLocation)

        assertEquals(expected, shoppingListItem.uniqueId)
    }

}