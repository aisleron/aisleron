package com.aisleron.di

import com.aisleron.ui.about.AboutViewModel
import com.aisleron.ui.product.ProductViewModel
import com.aisleron.ui.settings.SettingsViewModel
import com.aisleron.ui.shop.ShopViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import com.aisleron.ui.shoppinglist.ShoppingListViewModel
import com.aisleron.ui.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    includes(useCaseModule)

    viewModel {
        ShoppingListViewModel(
            getShoppingListUseCase = get(),
            updateProductStatusUseCase = get(),
            addAisleUseCase = get(),
            updateAisleUseCase = get(),
            updateAisleProductRankUseCase = get(),
            updateAisleRankUseCase = get(),
            removeAisleUseCase = get(),
            removeProductUseCase = get(),
            getAisleUseCase = get()
        )
    }

    viewModel {
        ShopViewModel(
            addLocationUseCase = get(),
            updateLocationUseCase = get(),
            getLocationUseCase = get()
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
            getProductUseCase = get()
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
            createSampleDataUseCase = get()
        )
    }
}