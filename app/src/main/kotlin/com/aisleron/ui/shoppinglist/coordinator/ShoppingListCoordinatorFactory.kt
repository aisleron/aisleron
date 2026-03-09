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

import com.aisleron.domain.aisle.usecase.ExpandCollapseAislesForLocationUseCase
import com.aisleron.domain.aisle.usecase.GetAislesForLocationUseCase
import com.aisleron.domain.location.usecase.ExpandCollapseLocationsUseCase
import com.aisleron.domain.location.usecase.SortLocationByNameUseCase
import com.aisleron.domain.location.usecase.SortLocationTypeByNameUseCase
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import com.aisleron.ui.shoppinglist.ShoppingListGrouping
import com.aisleron.ui.shoppinglist.ShoppingListItemViewModelFactory

class ShoppingListCoordinatorFactory(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val shoppingListItemViewModelFactory: ShoppingListItemViewModelFactory,
    private val expandCollapseAislesForLocationUseCase: ExpandCollapseAislesForLocationUseCase,
    private val sortLocationByNameUseCase: SortLocationByNameUseCase,
    private val getAislesForLocationUseCase: GetAislesForLocationUseCase,
    private val getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase,
    private val expandCollapseLocationsUseCase: ExpandCollapseLocationsUseCase,
    private val sortLocationTypeByNameUseCase: SortLocationTypeByNameUseCase
) {
    fun create(grouping: ShoppingListGrouping): ShoppingListCoordinator {
        return when (grouping) {
            is ShoppingListGrouping.AisleGrouping ->
                AisleListCoordinator(
                    getShoppingListUseCase = getShoppingListUseCase,
                    expandCollapseAislesForLocationUseCase = expandCollapseAislesForLocationUseCase,
                    shoppingListItemViewModelFactory = shoppingListItemViewModelFactory,
                    sortLocationByNameUseCase = sortLocationByNameUseCase,
                    getAislesForLocationUseCase = getAislesForLocationUseCase,
                    getLoyaltyCardForLocationUseCase = getLoyaltyCardForLocationUseCase,
                    locationId = grouping.locationId
                )

            is ShoppingListGrouping.LocationGrouping ->
                LocationListCoordinator(
                    getShoppingListUseCase = getShoppingListUseCase,
                    expandCollapseLocationsUseCase = expandCollapseLocationsUseCase,
                    shoppingListItemViewModelFactory = shoppingListItemViewModelFactory,
                    sortLocationTypeByNameUseCase = sortLocationTypeByNameUseCase,
                    locationType = grouping.locationType
                )
        }
    }
}