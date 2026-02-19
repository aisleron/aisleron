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

package com.aisleron.di

import com.aisleron.ui.shoppinglist.ShoppingListItemViewModelFactory
import com.aisleron.ui.shoppinglist.coordinator.ShoppingListCoordinatorFactory
import org.koin.dsl.module

val factoryModule = module {
    factory<ShoppingListItemViewModelFactory> {
        ShoppingListItemViewModelFactory(
            updateAisleRankUseCase = get(),
            removeAisleUseCase = get(),
            updateAisleProductRankUseCase = get(),
            updateAisleExpandedUseCase = get(),
            removeProductUseCase = get(),
            updateProductStatusUseCase = get(),
            updateProductQtyNeededUseCase = get(),
            changeProductAisleUseCase = get(),
            removeLocationUseCase = get(),
            updateLocationRankUseCase = get(),
            updateLocationExpandedUseCase = get()
        )
    }

    factory<ShoppingListCoordinatorFactory> {
        ShoppingListCoordinatorFactory(
            getShoppingListUseCase = get(),
            shoppingListItemViewModelFactory = get(),
            expandCollapseAislesForLocationUseCase = get(),
            getAislesForLocationUseCase = get(),
            sortLocationByNameUseCase = get(),
            getLoyaltyCardForLocationUseCase = get(),
            expandCollapseLocationsUseCase = get(),
            sortLocationTypeByNameUseCase = get()
        )
    }
}