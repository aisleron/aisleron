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
import com.aisleron.domain.aisle.usecase.ExpandCollapseAislesForLocationUseCase
import com.aisleron.domain.aisle.usecase.GetAislesForLocationUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.SortLocationByNameUseCase
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.shoppinglist.ShoppingListFilter
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import com.aisleron.ui.bundles.AisleListEntry
import com.aisleron.ui.shoppinglist.EmptyShoppingListItem
import com.aisleron.ui.shoppinglist.ShoppingListItem
import com.aisleron.ui.shoppinglist.ShoppingListItemViewModelFactory
import com.aisleron.ui.shoppinglist.ShoppingListViewModel.ListTitle
import com.aisleron.ui.shoppinglist.ShoppingListViewModel.ShoppingListEvent
import com.aisleron.ui.shoppinglist.ShoppingListViewModel.ShoppingListUiState
import kotlinx.coroutines.flow.map

class AisleListCoordinator(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val expandCollapseAislesForLocationUseCase: ExpandCollapseAislesForLocationUseCase,
    private val shoppingListItemViewModelFactory: ShoppingListItemViewModelFactory,
    private val sortLocationByNameUseCase: SortLocationByNameUseCase,
    private val getAislesForLocationUseCase: GetAislesForLocationUseCase,
    private val getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase,
    private val locationId: Int
) : ShoppingListCoordinator {
    override fun getShoppingListState(
        filters: ShoppingListFilter, selections: Set<ShoppingListItem.UniqueId>
    ) = getShoppingListUseCase(locationId, filters)
        .map { collectedLocation ->
            val listItems = mapShoppingList(
                collectedLocation, filters.productNameQuery.isNotBlank(), selections
            )

            val state: ShoppingListUiState = ShoppingListUiState.Updated(
                shoppingList = listItems,
                title = getListTitle(collectedLocation, filters.productFilter),
                showEditShop = collectedLocation?.type == LocationType.SHOP,
                manageAisles = true,
                showLoyaltyCard = getLoyaltyCardForLocationUseCase(locationId) != null
            )

            state
        }

    override suspend fun expandCollapseHeaders(expand: Boolean) {
        expandCollapseAislesForLocationUseCase(locationId, expand)
    }

    override suspend fun sortByName() {
        sortLocationByNameUseCase(locationId)
    }

    suspend fun requestLocationAisles(): List<AisleListEntry> =
        getAislesForLocationUseCase(locationId)
            .sortedBy { it.rank }
            .map { AisleListEntry(it.id, it.name) }

    fun navigateToEditShopEvent(): ShoppingListEvent =
        ShoppingListEvent.NavigateToEditLocation(locationId)

    suspend fun navigateToLoyaltyCardEvent(): ShoppingListEvent {
        val loyaltyCard = getLoyaltyCardForLocationUseCase(locationId)
        return ShoppingListEvent.NavigateToLoyaltyCard(loyaltyCard)
    }

    fun navigateToAddSingleAisleEvent(): ShoppingListEvent =
        ShoppingListEvent.NavigateToAddSingleAisle(locationId)

    fun navigateToAddMultipleAislesEvent(): ShoppingListEvent =
        ShoppingListEvent.NavigateToAddMultipleAisles(locationId)

    private fun mapShoppingList(
        location: Location?,
        showAllProducts: Boolean,
        selections: Set<ShoppingListItem.UniqueId>
    ): List<ShoppingListItem> {
        val filteredList: MutableList<ShoppingListItem> = location?.let { l ->
            l.aisles.flatMap { a ->
                listOf(
                    shoppingListItemViewModelFactory.createAisleItemViewModel(
                        a, selections
                    )
                ) + a.products.filter { (a.expanded || showAllProducts) }
                    .map { ap ->
                        shoppingListItemViewModelFactory.createProductItemViewModel(
                            ap, a.rank, location.id, selections
                        )
                    }
            }
        }?.toMutableList() ?: mutableListOf()

        if (filteredList.isEmpty()) {
            filteredList.add(EmptyShoppingListItem())
        }

        return filteredList.toList()
    }

    private fun getListTitle(collectedLocation: Location?, productFilter: FilterType): ListTitle =
        when (productFilter) {
            FilterType.IN_STOCK -> ListTitle.InStock
            FilterType.NEEDED -> ListTitle.Needed
            FilterType.ALL -> ListTitle.AllItems
        }.let { baseTitle ->
            if (collectedLocation == null || collectedLocation.type == LocationType.HOME) {
                baseTitle
            } else {
                ListTitle.LocationName(collectedLocation.name)
            }
        }
}