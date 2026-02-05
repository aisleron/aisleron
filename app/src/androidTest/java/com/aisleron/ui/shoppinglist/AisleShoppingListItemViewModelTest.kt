/*
 * Copyright (C) 2025-2026 aisleron.com
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
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.location.LocationRepository
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

class AisleShoppingListItemViewModelTest : KoinTest {
    private val aisleRepository: AisleRepository by lazy { get<AisleRepository>() }


    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private fun getAisleShoppingListItemViewModel(existingAisle: Aisle): AisleShoppingListItemViewModel {
        return AisleShoppingListItemViewModel(
            aisle = existingAisle,
            selected = false,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>(),
            updateAisleExpandedUseCase = get<UpdateAisleExpandedUseCase>()
        )
    }

    private suspend fun getAisle(): Aisle {
        val existingLocationId = get<LocationRepository>().getAll().first().id
        return aisleRepository.getAll()
            .last { it.locationId == existingLocationId && !it.isDefault }
    }

    @Test
    fun removeItem_ItemIsStandardAisle_AisleRemoved() = runTest {
        val existingAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(existingAisle)

        shoppingListItem.remove()

        val removedAisle = aisleRepository.get(existingAisle.id)
        assertNull(removedAisle)
    }

    @Test
    fun removeItem_ItemIsInvalidAisle_NoAisleRemoved() = runTest {
        val aisle = Aisle(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            locationId = -1,
            expanded = true,
            products = emptyList()
        )

        val shoppingListItem = getAisleShoppingListItemViewModel(aisle)
        val aisleCountBefore = aisleRepository.getAll().count()

        shoppingListItem.remove()

        val aisleCountAfter = aisleRepository.getAll().count()
        assertEquals(aisleCountBefore, aisleCountAfter)
    }

    @Test
    fun updateItemRank_AisleMoved_AisleRankUpdated() = runTest {
        val movedAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(movedAisle)
        val precedingAisle = aisleRepository.getAll()
            .first { it.locationId == movedAisle.locationId && !it.isDefault && it.id != movedAisle.id }

        val precedingItem = getAisleShoppingListItemViewModel(precedingAisle)

        shoppingListItem.updateRank(precedingItem)

        val updatedAisle = aisleRepository.get(movedAisle.id)
        assertEquals(precedingItem.rank + 1, updatedAisle?.rank)
    }

    @Test
    fun updateItemRank_NullPrecedingItem_AisleRankIsOne() = runTest {
        val movedAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(movedAisle)

        shoppingListItem.updateRank(null)

        val updatedAisle = aisleRepository.get(movedAisle.id)
        assertEquals(1, updatedAisle?.rank)
    }

    private suspend fun updateExpandedArrangeActAssert(expanded: Boolean) {
        val aisle = getAisle().copy(expanded = !expanded)
        aisleRepository.update(aisle)

        val shoppingListItem = getAisleShoppingListItemViewModel(aisle)

        shoppingListItem.updateExpanded(expanded)

        val updatedAisle = aisleRepository.get(aisle.id)
        assertEquals(expanded, updatedAisle?.expanded)
    }

    @Test
    fun updateExpanded_ExpandedTrue_AisleUpdatedToExpanded() = runTest {
        updateExpandedArrangeActAssert(true)
    }

    @Test
    fun updateExpanded_ExpandedFalse_AisleUpdatedToNotExpanded() = runTest {
        updateExpandedArrangeActAssert(false)
    }

    @Test
    fun onCreate_PropertiesInitializedCorrectly() = runTest {
        val existingAisle = getAisle()

        val shoppingListItem = getAisleShoppingListItemViewModel(existingAisle)

        assertEquals(existingAisle.id, shoppingListItem.id)
        assertEquals(existingAisle.name, shoppingListItem.name)
        assertEquals(existingAisle.rank, shoppingListItem.rank)
        assertEquals(existingAisle.locationId, shoppingListItem.locationId)
        assertEquals(existingAisle.isDefault, shoppingListItem.isDefault)
        assertEquals(existingAisle.expanded, shoppingListItem.expanded)
        assertEquals(existingAisle.products.count(), shoppingListItem.childCount)
    }

    @Test
    fun navigateToEditEvent_ReturnsNavigateToEditAisleEvent() = runTest {
        val existingAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(existingAisle)

        val event = shoppingListItem.editNavigationEvent()

        assertEquals(
            ShoppingListViewModel.ShoppingListEvent.NavigateToEditAisle(existingAisle.id),
            event
        )
    }
}