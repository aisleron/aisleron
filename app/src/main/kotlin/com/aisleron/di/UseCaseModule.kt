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

import com.aisleron.data.maintenance.DatabaseMaintenanceImpl
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisle.usecase.IsAisleNameUniqueUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCaseImpl
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.GetAisleMaxRankUseCase
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
import com.aisleron.domain.location.usecase.CopyLocationUseCase
import com.aisleron.domain.location.usecase.CopyLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.GetPinnedShopsUseCase
import com.aisleron.domain.location.usecase.GetShopsUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCaseImpl
import com.aisleron.domain.location.usecase.SortLocationByNameUseCase
import com.aisleron.domain.location.usecase.SortLocationByNameUseCaseImpl
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardToLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardToLocationUseCaseImpl
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCaseImpl
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCaseImpl
import com.aisleron.domain.loyaltycard.usecase.RemoveLoyaltyCardFromLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.RemoveLoyaltyCardFromLocationUseCaseImpl
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.CopyProductUseCase
import com.aisleron.domain.product.usecase.CopyProductUseCaseImpl
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCaseImpl
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

    factory<SortLocationByNameUseCase> {
        SortLocationByNameUseCaseImpl(
            locationRepository = get(),
            updateAisleUseCase = get(),
            updateAisleProductUseCase = get()
        )
    }

    factory<CopyLocationUseCase> {
        CopyLocationUseCaseImpl(
            locationRepository = get(),
            aisleRepository = get(),
            aisleProductRepository = get(),
            isLocationNameUniqueUseCase = get()
        )
    }

    /**
     * Aisle Use Cases
     */
    factory<GetAisleUseCase> { GetAisleUseCaseImpl(aisleRepository = get()) }
    factory<GetDefaultAislesUseCase> { GetDefaultAislesUseCase(aisleRepository = get()) }
    factory<UpdateAisleRankUseCase> { UpdateAisleRankUseCase(aisleRepository = get()) }
    factory<IsAisleNameUniqueUseCase> { IsAisleNameUniqueUseCase(aisleRepository = get()) }

    factory<AddAisleUseCase> {
        AddAisleUseCaseImpl(
            aisleRepository = get(),
            getLocationUseCase = get(),
            isAisleNameUniqueUseCase = get()
        )
    }

    factory<UpdateAisleUseCase> {
        UpdateAisleUseCaseImpl(
            aisleRepository = get(),
            getLocationUseCase = get(),
            isAisleNameUniqueUseCase = get()
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

    factory<UpdateAisleExpandedUseCase> {
        UpdateAisleExpandedUseCaseImpl(
            getAisleUseCase = get(),
            updateAisleUseCase = get()
        )
    }

    /**
     * Aisle Product Use Cases
     */
    factory<AddAisleProductsUseCase> { AddAisleProductsUseCase(aisleProductRepository = get()) }
    factory<UpdateAisleProductsUseCase> { UpdateAisleProductsUseCase(aisleProductRepository = get()) }
    factory<UpdateAisleProductRankUseCase> { UpdateAisleProductRankUseCase(aisleProductRepository = get()) }
    factory<RemoveProductsFromAisleUseCase> { RemoveProductsFromAisleUseCase(aisleProductRepository = get()) }
    factory<GetAisleMaxRankUseCase> { GetAisleMaxRankUseCase(aisleProductRepository = get()) }

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
            isProductNameUniqueUseCase = get(),
            getAisleMaxRankUseCase = get()
        )
    }

    factory<UpdateProductStatusUseCase> {
        UpdateProductStatusUseCaseImpl(
            getProductUseCase = get(),
            updateProductUseCase = get()
        )
    }

    factory<UpdateProductQtyNeededUseCase> {
        UpdateProductQtyNeededUseCaseImpl(
            getProductUseCase = get(),
            updateProductUseCase = get()
        )
    }

    factory<CopyProductUseCase> {
        CopyProductUseCaseImpl(
            productRepository = get(),
            aisleProductRepository = get(),
            isProductNameUniqueUseCase = get()
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

    /**
     * Loyalty Card Use Cases
     */
    factory<AddLoyaltyCardUseCase> { AddLoyaltyCardUseCaseImpl(loyaltyCardRepository = get()) }
    factory<AddLoyaltyCardToLocationUseCase> {
        AddLoyaltyCardToLocationUseCaseImpl(
            loyaltyCardRepository = get(),
            getLocationUseCase = get()
        )
    }

    factory<RemoveLoyaltyCardFromLocationUseCase> {
        RemoveLoyaltyCardFromLocationUseCaseImpl(loyaltyCardRepository = get())
    }

    factory<GetLoyaltyCardForLocationUseCase> {
        GetLoyaltyCardForLocationUseCaseImpl(loyaltyCardRepository = get())
    }
}