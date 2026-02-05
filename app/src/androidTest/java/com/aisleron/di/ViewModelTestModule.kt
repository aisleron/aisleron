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

package com.aisleron.di

import com.aisleron.ui.about.AboutViewModel
import com.aisleron.ui.aisle.AisleViewModel
import com.aisleron.ui.copyentity.CopyEntityViewModel
import com.aisleron.ui.note.NoteDialogViewModel
import com.aisleron.ui.product.ProductViewModel
import com.aisleron.ui.settings.SettingsViewModel
import com.aisleron.ui.shop.ShopViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import com.aisleron.ui.shoppinglist.ShoppingListViewModel
import com.aisleron.ui.welcome.WelcomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
val viewModelTestModule = module {
    viewModel {
        ShoppingListViewModel(
            getShoppingListUseCase = get(),
            sortLocationByNameUseCase = get(),
            getLoyaltyCardForLocationUseCase = get(),
            expandCollapseAislesForLocationUseCase = get(),
            getAislesForLocationUseCase = get(),
            shoppingListItemViewModelFactory = get(),
            debounceTime = 0,
            TestScope(UnconfinedTestDispatcher())
        )
    }

    viewModel {
        ShopViewModel(
            addLocationUseCase = get(),
            updateLocationUseCase = get(),
            addLoyaltyCardUseCase = get(),
            addLoyaltyCardToLocationUseCase = get(),
            removeLoyaltyCardFromLocationUseCase = get(),
            getLoyaltyCardForLocationUseCase = get(),
            getNoteParentUseCase = get(),
            applyNoteChangesUseCase = get(),
            getLocationMaxRankUseCase = get(),
            TestScope(UnconfinedTestDispatcher())
        )
    }

    viewModel {
        ShopListViewModel(
            getShopsUseCase = get(),
            getPinnedShopsUseCase = get(),
            removeLocationUseCase = get(),
            getLocationUseCase = get(),
            TestScope(UnconfinedTestDispatcher())
        )
    }

    viewModel {
        ProductViewModel(
            addProductUseCase = get(),
            updateProductUseCase = get(),
            getAisleUseCase = get(),
            getNoteParentUseCase = get(),
            applyNoteChangesUseCase = get(),
            getProductMappingsUseCase = get(),
            getAislesForLocationUseCase = get(),
            changeProductAisleUseCase = get(),
            TestScope(UnconfinedTestDispatcher())
        )
    }

    viewModel {
        SettingsViewModel(
            backupDatabaseUseCase = get(),
            restoreDatabaseUseCase = get(),
            getHomeLocationUseCase = get(),
            getPinnedShopsUseCase = get(),
            TestScope(UnconfinedTestDispatcher())
        )
    }

    viewModel {
        AboutViewModel()
    }

    viewModel {
        AisleViewModel(
            addAisleUseCase = get(),
            updateAisleUseCase = get(),
            getAisleUseCase = get(),
            getAisleMaxRankUseCase = get(),
            TestScope(UnconfinedTestDispatcher())
        )
    }

    viewModel {
        WelcomeViewModel(
            createSampleDataUseCase = get(),
            getAllProductsUseCase = get(),
            TestScope(UnconfinedTestDispatcher())
        )
    }

    viewModel {
        CopyEntityViewModel(
            copyLocationUseCase = get(),
            copyProductUseCase = get(),
            getProductUseCase = get(),
            getLocationUseCase = get(),
            TestScope(UnconfinedTestDispatcher())
        )
    }

    viewModel {
        NoteDialogViewModel(
            getNoteParentUseCase = get(),
            applyNoteChangesUseCase = get(),
            TestScope(UnconfinedTestDispatcher())
        )
    }
}