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
import com.aisleron.domain.aisle.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.GetProductsUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
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
            getProductsUseCase = get(),
            addAisleProductsUseCase = get()
        )
    }

    factory<UpdateLocationUseCase> { UpdateLocationUseCase(locationRepository = get()) }

    factory<GetLocationUseCase> { GetLocationUseCase(locationRepository = get()) }

    /**
     * Aisle Use Cases
     */
    factory<AddAisleUseCase> { AddAisleUseCase(aisleRepository = get()) }

    factory<AddAisleProductsUseCase> { AddAisleProductsUseCase(aisleRepository = get()) }

    factory<GetDefaultAislesUseCase> { GetDefaultAislesUseCase(aisleRepository = get()) }

    /**
     * Product Use Cases
     */
    factory<GetProductsUseCase> { GetProductsUseCase(productRepository = get()) }

    factory<GetProductUseCase> { GetProductUseCase(productRepository = get()) }

    factory<AddProductUseCase> {
        AddProductUseCase(
            productRepository = get(),
            getDefaultAislesUseCase = get(),
            addAisleProductsUseCase = get()
        )
    }

    factory<UpdateProductUseCase> { UpdateProductUseCase(productRepository = get()) }

    /**
     * ViewModels
     */
    viewModel { ShoppingListViewModel(get(), get()) }

    viewModel {
        ShopViewModel(
            addLocationUseCase = get(),
            updateLocationUseCase = get(),
            getLocationUseCase = get()
        )
    }

    viewModel { ShopListViewModel(get()) }

    viewModel {
        ProductViewModel(
            addProductUseCase = get(),
            updateProductUseCase = get(),
            getProductUseCase = get()
        )
    }
}