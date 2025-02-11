package com.aisleron.di

import com.aisleron.data.maintenance.DatabaseMaintenanceImpl
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCase
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCaseImpl
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCase
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCaseImpl
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.AddLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.GetPinnedShopsUseCase
import com.aisleron.domain.location.usecase.GetShopsUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCaseImpl
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCaseImpl
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCaseImpl
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val useCaseModule = module {

    /**
     * Location Use Cases
     */
    factory<GetLocationUseCase> { GetLocationUseCase(locationRepository = get<LocationRepository>()) }
    factory<GetShopsUseCase> { GetShopsUseCase(locationRepository = get()) }
    factory<GetPinnedShopsUseCase> { GetPinnedShopsUseCase(locationRepository = get()) }
    factory<IsLocationNameUniqueUseCase> { IsLocationNameUniqueUseCase(locationRepository = get()) }
    factory<GetHomeLocationUseCase> { GetHomeLocationUseCase(locationRepository = get()) }

    factory<UpdateLocationUseCase> {
        UpdateLocationUseCase(
            locationRepository = get(),
            isLocationNameUniqueUseCase = get()
        )
    }

    factory<RemoveLocationUseCase> {
        RemoveLocationUseCaseImpl(
            locationRepository = get(),
            removeAisleUseCase = get(),
            removeDefaultAisleUseCase = get()
        )
    }

    factory<AddLocationUseCase> {
        AddLocationUseCaseImpl(
            locationRepository = get(),
            addAisleUseCase = get(),
            getAllProductsUseCase = get(),
            addAisleProductsUseCase = get(),
            isLocationNameUniqueUseCase = get()
        )
    }

    /**
     * Aisle Use Cases
     */
    factory<GetAisleUseCase> { GetAisleUseCaseImpl(aisleRepository = get()) }
    factory<GetDefaultAislesUseCase> { GetDefaultAislesUseCase(aisleRepository = get()) }
    factory<UpdateAisleRankUseCase> { UpdateAisleRankUseCase(aisleRepository = get()) }

    factory<AddAisleUseCase> {
        AddAisleUseCaseImpl(
            aisleRepository = get(),
            getLocationUseCase = get()
        )
    }

    factory<UpdateAisleUseCase> {
        UpdateAisleUseCaseImpl(
            aisleRepository = get(),
            getLocationUseCase = get()
        )
    }

    factory<RemoveDefaultAisleUseCase> {
        RemoveDefaultAisleUseCase(
            aisleRepository = get(),
            removeProductsFromAisleUseCase = get()
        )
    }

    factory<RemoveAisleUseCase> {
        RemoveAisleUseCaseImpl(
            aisleRepository = get(),
            updateAisleProductsUseCase = get(),
            removeProductsFromAisleUseCase = get()
        )
    }

    /**
     * Aisle Product Use Cases
     */
    factory<AddAisleProductsUseCase> { AddAisleProductsUseCase(aisleProductRepository = get()) }
    factory<UpdateAisleProductsUseCase> { UpdateAisleProductsUseCase(aisleProductRepository = get()) }
    factory<UpdateAisleProductRankUseCase> { UpdateAisleProductRankUseCase(aisleProductRepository = get()) }
    factory<RemoveProductsFromAisleUseCase> { RemoveProductsFromAisleUseCase(aisleProductRepository = get()) }

    /**
     * Product Use Cases
     */
    factory<GetAllProductsUseCase> { GetAllProductsUseCase(productRepository = get()) }
    factory<GetProductUseCase> { GetProductUseCase(productRepository = get()) }
    factory<RemoveProductUseCase> { RemoveProductUseCase(productRepository = get()) }
    factory<IsProductNameUniqueUseCase> { IsProductNameUniqueUseCase(productRepository = get()) }

    factory<UpdateProductUseCase> {
        UpdateProductUseCase(
            productRepository = get(),
            isProductNameUniqueUseCase = get()
        )
    }

    factory<AddProductUseCase> {
        AddProductUseCaseImpl(
            productRepository = get(),
            getDefaultAislesUseCase = get(),
            addAisleProductsUseCase = get(),
            isProductNameUniqueUseCase = get()
        )
    }

    factory<UpdateProductStatusUseCase> {
        UpdateProductStatusUseCaseImpl(
            getProductUseCase = get(),
            updateProductUseCase = get()
        )
    }

    /**
     * Shopping List Use Cases
     */
    factory<GetShoppingListUseCase> { GetShoppingListUseCase(locationRepository = get()) }

    /**
     * Backup Use Cases
     */
    factory<BackupDatabaseUseCase> { BackupDatabaseUseCaseImpl(databaseMaintenance = get()) }
    factory<RestoreDatabaseUseCase> { RestoreDatabaseUseCaseImpl(databaseMaintenance = get()) }

    factory<DatabaseMaintenance> {
        DatabaseMaintenanceImpl(database = get(), context = androidApplication())
    }

    /**
     * Sample Data Use Cases
     */
    factory<CreateSampleDataUseCase> {
        CreateSampleDataUseCaseImpl(
            addProductUseCase = get(),
            addAisleUseCase = get(),
            getShoppingListUseCase = get(),
            updateAisleProductRankUseCase = get(),
            addLocationUseCase = get(),
            getAllProductsUseCase = get(),
            getHomeLocationUseCase = get()
        )
    }
}