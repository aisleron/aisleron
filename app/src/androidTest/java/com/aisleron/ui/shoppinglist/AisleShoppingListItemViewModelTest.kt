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
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get

class AisleShoppingListItemViewModelTest : KoinTest {

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
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = existingAisle.locationId,
            expanded = existingAisle.expanded,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )
    }

    private fun getAisle(): Aisle {
        return runBlocking {
            val existingLocationId = get<LocationRepository>().getAll().first().id
            get<AisleRepository>().getAll()
                .last { it.locationId == existingLocationId && !it.isDefault }
        }
    }

    @Test
    fun removeItem_ItemIsStandardAisle_AisleRemoved() = runTest {
        val existingAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(existingAisle)

        runBlocking { shoppingListItem.remove() }

        val removedAisle = get<AisleRepository>().get(existingAisle.id)
        Assert.assertNull(removedAisle)
    }

    @Test
    fun removeItem_ItemIsInvalidAisle_NoAisleRemoved() = runTest {
        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = -1,
            expanded = true,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()
        shoppingListItem.remove()
        val aisleCountAfter = aisleRepository.getAll().count()

        Assert.assertEquals(aisleCountBefore, aisleCountAfter)
    }

    @Test
    fun updateItemRank_AisleMoved_AisleRankUpdated() = runTest {
        val movedAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(movedAisle)
        val aisleRepository = get<AisleRepository>()
        val precedingAisle = aisleRepository.getAll()
            .first { it.locationId == movedAisle.locationId && !it.isDefault && it.id != movedAisle.id }


        val precedingItem = getAisleShoppingListItemViewModel(precedingAisle)

        runBlocking { shoppingListItem.updateRank(precedingItem) }

        val updatedAisle = aisleRepository.get(movedAisle.id)
        Assert.assertEquals(precedingItem.rank + 1, updatedAisle?.rank)
    }

    @Test
    fun updateItemRank_NullPrecedingItem_AisleRankIsOne() = runTest {
        val movedAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(movedAisle)

        shoppingListItem.updateRank(null)

        val updatedAisle = get<AisleRepository>().get(movedAisle.id)

        Assert.assertEquals(1, updatedAisle?.rank)
    }
}