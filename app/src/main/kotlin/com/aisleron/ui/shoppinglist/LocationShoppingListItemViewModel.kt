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

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCase

class LocationShoppingListItemViewModel(
    private val location: Location,
    override val selected: Boolean,
    // private val updateLocationRankUseCase: UpdateLocationRankUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val removeLocationUseCase: RemoveLocationUseCase,
    // private val updateLocationExpandedUseCase: UpdateLocationExpandedUseCase
) : LocationShoppingListItem, HeaderShoppingListItemViewModel {
    override val childCount: Int get() = location.aisles.sumOf { it.products.size }
    override val isDefault: Boolean get() = false
    override val expanded: Boolean get() = location.expanded
    override val rank: Int get() = location.rank
    override val id: Int get() = location.id
    override val name: String get() = location.name
    override val aisleId: Int get() = location.aisles.firstOrNull { !it.isDefault }?.id ?: 0

    override suspend fun remove() {
        val removeLocation = getLocationUseCase(id)
        removeLocation?.let { removeLocationUseCase(it) }
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        // TODO: Add updateLocationRankUseCase
        /*updateLocationRankUseCase(
            location.copy(
                rank = precedingItem?.let { it.headerRank + 1 } ?: 1
            )
        )*/
    }

    override fun editNavigationEvent(): ShoppingListViewModel.ShoppingListEvent =
        ShoppingListViewModel.ShoppingListEvent.NavigateToEditLocation(id)

    override suspend fun updateExpanded(expanded: Boolean) {
        // TODO: Add updateLocationExpandedUseCase
        //updateLocationExpandedUseCase(id, expanded)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocationShoppingListItemViewModel) return false

        if (location != other.location) return false
        if (selected != other.selected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + selected.hashCode()
        return result
    }
}
