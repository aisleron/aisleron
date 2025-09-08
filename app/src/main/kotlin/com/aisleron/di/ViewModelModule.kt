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

package com.aisleron.di

import com.aisleron.ui.about.AboutViewModel
import com.aisleron.ui.aisle.AisleViewModel
import com.aisleron.ui.copyentity.CopyEntityViewModel
import com.aisleron.ui.product.ProductViewModel
import com.aisleron.ui.settings.SettingsViewModel
import com.aisleron.ui.shop.ShopViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import com.aisleron.ui.shoppinglist.ShoppingListViewModel
import com.aisleron.ui.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        ShoppingListViewModel(
            getShoppingListUseCase = get(),
            updateProductStatusUseCase = get(),
            updateAisleProductRankUseCase = get(),
            updateAisleRankUseCase = get(),
            removeAisleUseCase = get(),
            removeProductUseCase = get(),
            getAisleUseCase = get(),
            updateAisleExpandedUseCase = get(),
            sortLocationByNameUseCase = get(),
            getLoyaltyCardForLocationUseCase = get(),
            updateProductQtyNeededUseCase = get()
        )
    }

    viewModel {
        ShopViewModel(
            addLocationUseCase = get(),
            updateLocationUseCase = get(),
            getLocationUseCase = get(),
            addLoyaltyCardUseCase = get(),
            addLoyaltyCardToLocationUseCase = get(),
            removeLoyaltyCardFromLocationUseCase = get(),
            getLoyaltyCardForLocationUseCase = get()
        )
    }

    viewModel {
        ShopListViewModel(
            getShopsUseCase = get(),
            getPinnedShopsUseCase = get(),
            removeLocationUseCase = get(),
            getLocationUseCase = get()
        )
    }

    viewModel {
        ProductViewModel(
            addProductUseCase = get(),
            updateProductUseCase = get(),
            getProductUseCase = get(),
            getAisleUseCase = get()
        )
    }

    viewModel {
        SettingsViewModel(
            backupDatabaseUseCase = get(),
            restoreDatabaseUseCase = get()
        )
    }

    viewModel {
        AboutViewModel()
    }

    viewModel {
        WelcomeViewModel(
            createSampleDataUseCase = get(),
            getAllProductsUseCase = get()
        )
    }

    viewModel {
        AisleViewModel(
            addAisleUseCase = get(),
            updateAisleUseCase = get()
        )
    }

    viewModel {
        CopyEntityViewModel(
            copyLocationUseCase = get(),
            copyProductUseCase = get(),
            getProductUseCase = get(),
            getLocationUseCase = get()
        )
    }
}