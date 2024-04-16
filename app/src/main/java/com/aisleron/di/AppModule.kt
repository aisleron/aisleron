package com.aisleron.di

import androidx.room.Room
import com.aisleron.data.AisleronDatabase
import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.aisle.AisleRepositoryImpl
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecases.AddAisleUseCase
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecases.AddLocationUseCase
import com.aisleron.domain.location.usecases.GetLocationUseCase
import com.aisleron.domain.location.usecases.UpdateLocationUseCase
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecases.GetProductsUseCase
import com.aisleron.ui.product.ProductViewModel
import com.aisleron.ui.shop.ShopViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import com.aisleron.ui.shoppinglist.ShoppingListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AisleronDatabase::class.java,
            "aisleron.db"
        ).build()
    }

    /**
     * Repositories
     */
    factory<LocationRepository> {
        LocationRepositoryImpl(get(), LocationMapper())
    }

    factory<ProductRepository> {
        ProductRepositoryImpl(get(), ProductMapper())
    }

    factory<AisleRepository> {
        AisleRepositoryImpl(
            db = get(),
            aisleMapper = AisleMapper()
        )
    }

    /**
     * Location Use Cases
     */
    factory<AddLocationUseCase> {
        AddLocationUseCase(
            locationRepository = get(),
            addAisleUseCase = get(),
            getProductsUseCase = get()
        )
    }

    factory<UpdateLocationUseCase> {
        UpdateLocationUseCase(locationRepository = get())
    }

    factory<GetLocationUseCase> {
        GetLocationUseCase(locationRepository = get())
    }

    /**
     * Aisle Use Cases
     */
    factory<AddAisleUseCase> {
        AddAisleUseCase(aisleRepository = get())
    }

    /**
     * Product Use Cases
     */
    factory<GetProductsUseCase> {
        GetProductsUseCase(productRepository = get())
    }

    /**
     * ViewModels
     */
    viewModel { ShoppingListViewModel(get(), get()) }

    viewModel {
        ShopViewModel(
            addLocation = get(),
            updateLocation = get(),
            getLocation = get()
        )
    }

    viewModel { ShopListViewModel(get()) }

    viewModel { ProductViewModel(get()) }
}