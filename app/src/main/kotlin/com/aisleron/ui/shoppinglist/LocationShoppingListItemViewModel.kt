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

import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationExpandedUseCase
import com.aisleron.domain.location.usecase.UpdateLocationRankUseCase
import com.aisleron.ui.copyentity.CopyEntityType
import com.aisleron.ui.note.NoteParentRef

data class LocationShoppingListItemViewModel(
    override val selected: Boolean,
    override val childCount: Int,
    override val isDefault: Boolean = false,
    override val expanded: Boolean,
    override val rank: Int,
    override val id: Int,
    override val name: String,
    override val aisleId: Int,
) : LocationShoppingListItem, HeaderShoppingListItemViewModel {
    lateinit var removeLocationUseCase: RemoveLocationUseCase
    lateinit var updateLocationRankUseCase: UpdateLocationRankUseCase
    lateinit var updateLocationExpandedUseCase: UpdateLocationExpandedUseCase

    override val uniqueId: ShoppingListItem.UniqueId
        get() = ShoppingListItem.UniqueId(itemType, id)

    override suspend fun remove() {
        removeLocationUseCase(id)
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        updateLocationRankUseCase(
            id, precedingItem?.let { it.headerRank + 1 } ?: 1
        )
    }

    override fun editNavigationEvent(): ShoppingListViewModel.ShoppingListEvent =
        ShoppingListViewModel.ShoppingListEvent.NavigateToEditLocation(id)

    override fun copyDialogNavigationEvent(): ShoppingListViewModel.ShoppingListEvent {
        return ShoppingListViewModel.ShoppingListEvent.NavigateToCopyDialogEvent(
            entityType = CopyEntityType.Location(id),
            name = name
        )
    }

    override fun noteDialogNavigationEvent(): ShoppingListViewModel.ShoppingListEvent {
        return ShoppingListViewModel.ShoppingListEvent.NavigateToNoteDialogEvent(
            parentRef = NoteParentRef.Location(id)
        )
    }

    override suspend fun updateExpanded(expanded: Boolean) {
        updateLocationExpandedUseCase(id, expanded)
    }
}
