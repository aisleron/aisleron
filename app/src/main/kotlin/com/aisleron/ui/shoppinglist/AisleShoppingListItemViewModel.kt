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

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase

class AisleShoppingListItemViewModel(
    private val aisle: Aisle,
    override val selected: Boolean,
    private val updateAisleRankUseCase: UpdateAisleRankUseCase,
    private val getAisleUseCase: GetAisleUseCase,
    private val removeAisleUseCase: RemoveAisleUseCase,
    private val updateAisleExpandedUseCase: UpdateAisleExpandedUseCase
) : AisleShoppingListItem, HeaderShoppingListItemViewModel {
    override val childCount: Int get() = aisle.products.count()
    override val locationId: Int get() = aisle.locationId
    override val isDefault: Boolean get() = aisle.isDefault
    override val expanded: Boolean get() = aisle.expanded
    override val rank: Int get() = aisle.rank
    override val id: Int get() = aisle.id
    override val name: String get() = aisle.name

    override suspend fun remove() {
        val aisle = getAisleUseCase(id)
        aisle?.let { removeAisleUseCase(it) }
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        updateAisleRankUseCase(
            aisle.copy(
                rank = precedingItem?.let { it.headerRank + 1 } ?: 1
            )
        )
    }

    override fun editNavigationEvent(): ShoppingListViewModel.ShoppingListEvent =
        ShoppingListViewModel.ShoppingListEvent.NavigateToEditAisle(id, locationId)

    override suspend fun updateExpanded(expanded: Boolean) {
        updateAisleExpandedUseCase(id, expanded)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AisleShoppingListItemViewModel) return false

        if (aisle != other.aisle) return false
        if (selected != other.selected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = aisle.hashCode()
        result = 31 * result + selected.hashCode()
        return result
    }
}
