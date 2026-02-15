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

package com.aisleron.ui.shoppinglist.coordinator

import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.ExpandCollapseLocationsUseCase
import com.aisleron.domain.shoppinglist.ShoppingListFilter
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import com.aisleron.ui.shoppinglist.EmptyShoppingListItem
import com.aisleron.ui.shoppinglist.ShoppingListItem
import com.aisleron.ui.shoppinglist.ShoppingListItemViewModelFactory
import com.aisleron.ui.shoppinglist.ShoppingListViewModel.ListTitle
import com.aisleron.ui.shoppinglist.ShoppingListViewModel.ShoppingListUiState
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class LocationListCoordinator(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val expandCollapseLocationsUseCase: ExpandCollapseLocationsUseCase,
    private val shoppingListItemViewModelFactory: ShoppingListItemViewModelFactory,
    private val locationType: LocationType
) : ShoppingListCoordinator {
    override fun getShoppingListState(
        filters: ShoppingListFilter,
        selections: Set<ShoppingListItem.UniqueId>
    ) = getShoppingListUseCase(locationType, filters)
        .map { collectedLocations ->
            val listItems = mapShoppingList(
                collectedLocations, filters.productNameQuery.isNotBlank(), selections
            )

            val state: ShoppingListUiState = ShoppingListUiState.Updated(
                shoppingList = listItems,
                title = getListTitle(locationType, filters.productFilter),
                showEditShop = false
            )

            state
        }

    override suspend fun expandCollapseHeaders(expand: Boolean) {
        expandCollapseLocationsUseCase(locationType, expand)
    }

    override suspend fun sortByName() {
        TODO("Not yet implemented")
    }

    private fun mapShoppingList(
        locations: List<Location>,
        showAllProducts: Boolean,
        selections: Set<ShoppingListItem.UniqueId>
    ): List<ShoppingListItem> {
        val filteredList: MutableList<ShoppingListItem> = locations.flatMap { location ->
            val locationHeader = shoppingListItemViewModelFactory.createLocationItemViewModel(
                location, selections
            )

            val productItems = location.aisles.flatMap { aisle ->
                aisle.products
                    .filter { location.expanded || showAllProducts }
                    .map { ap ->
                        shoppingListItemViewModelFactory.createProductItemViewModel(
                            ap, aisle.rank, location.id, selections
                        )
                    }
            }

            listOf(locationHeader) + productItems
        }.toMutableList()

        if (filteredList.isEmpty()) {
            filteredList.add(EmptyShoppingListItem())
        }

        return filteredList.toList()
    }

    private fun getListTitle(locationType: LocationType, productFilter: FilterType): ListTitle =
        when (locationType) {
            LocationType.SHOP -> ListTitle.AllShops
            LocationType.HOME ->
                when (productFilter) {
                    FilterType.ALL -> ListTitle.AllItems
                    FilterType.IN_STOCK -> ListTitle.InStock
                    FilterType.NEEDED -> ListTitle.Needed
                }
        }
}