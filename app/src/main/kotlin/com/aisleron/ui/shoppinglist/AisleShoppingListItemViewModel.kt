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

import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase

data class AisleShoppingListItemViewModel(
    override val selected: Boolean,
    override val childCount: Int,
    override val locationId: Int,
    override val isDefault: Boolean,
    override val expanded: Boolean,
    override val rank: Int,
    override val id: Int,
    override val name: String
) : AisleShoppingListItem, HeaderShoppingListItemViewModel {
    lateinit var updateAisleRankUseCase: UpdateAisleRankUseCase
    lateinit var removeAisleUseCase: RemoveAisleUseCase
    lateinit var updateAisleExpandedUseCase: UpdateAisleExpandedUseCase

    override val uniqueId: ShoppingListItem.UniqueId
        get() = ShoppingListItem.UniqueId(itemType, id)

    override suspend fun remove() {
        removeAisleUseCase(id)
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        val newRank = precedingItem?.let { it.headerRank + 1 } ?: 1
        updateAisleRankUseCase(id, newRank)
    }

    override fun editNavigationEvent(): ShoppingListViewModel.ShoppingListEvent =
        ShoppingListViewModel.ShoppingListEvent.NavigateToEditAisle(id, locationId)

    override suspend fun updateExpanded(expanded: Boolean) {
        updateAisleExpandedUseCase(id, expanded)
    }
}
