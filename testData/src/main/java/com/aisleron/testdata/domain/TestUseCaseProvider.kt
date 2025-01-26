package com.aisleron.domain

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCaseImpl
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCaseImpl
import com.aisleron.domain.location.usecase.AddLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.GetPinnedShopsUseCase
import com.aisleron.domain.location.usecase.GetShopsUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCaseImpl
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCaseImpl
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCaseImpl
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase

class TestUseCaseProvider(testData: TestDataManager) {

    /**
     * Location Use Cases
     */
    val getLocationUseCase = GetLocationUseCase(testData.locationRepository)
    val getShopsUseCase = GetShopsUseCase(testData.locationRepository)
    val getPinnedShopsUseCase = GetPinnedShopsUseCase(testData.locationRepository)
    val isLocationNameUniqueUseCase = IsLocationNameUniqueUseCase(testData.locationRepository)
    val updateLocationUseCase =
        UpdateLocationUseCase(testData.locationRepository, isLocationNameUniqueUseCase)
    val getHomeLocationUseCase = GetHomeLocationUseCase(testData.locationRepository)

    /**
     * Aisle Product Use Cases
     */
    val updateAisleProductsUseCase = UpdateAisleProductsUseCase(testData.aisleProductRepository)
    val removeProductsFromAisleUseCase =
        RemoveProductsFromAisleUseCase(testData.aisleProductRepository)
    val updateAisleProductRankUseCase =
        UpdateAisleProductRankUseCase(testData.aisleProductRepository)
    val addAisleProductsUseCase = AddAisleProductsUseCase(testData.aisleProductRepository)


    /**
     * Aisle Use Cases
     */
    val getAisleUseCase = GetAisleUseCaseImpl(testData.aisleRepository)
    val addAisleUseCase = AddAisleUseCaseImpl(testData.aisleRepository, getLocationUseCase)
    val updateAisleUseCase = UpdateAisleUseCaseImpl(testData.aisleRepository, getLocationUseCase)
    val updateAisleRankUseCase = UpdateAisleRankUseCase(testData.aisleRepository)
    val removeAisleUseCase = RemoveAisleUseCaseImpl(
        testData.aisleRepository, updateAisleProductsUseCase, removeProductsFromAisleUseCase
    )
    val removeDefaultAisleUseCase =
        RemoveDefaultAisleUseCase(testData.aisleRepository, removeProductsFromAisleUseCase)
    val getDefaultAislesUseCase = GetDefaultAislesUseCase(testData.aisleRepository)

    /**
     * Product Use Cases
     */
    val removeProductUseCase = RemoveProductUseCase(testData.productRepository)
    val getProductUseCase = GetProductUseCase(testData.productRepository)
    val getAllProductsUseCase = GetAllProductsUseCase(testData.productRepository)
    val isProductNameUniqueUseCase = IsProductNameUniqueUseCase(testData.productRepository)
    val updateProductUseCase =
        UpdateProductUseCase(testData.productRepository, isProductNameUniqueUseCase)
    val updateProductStatusUseCase =
        UpdateProductStatusUseCaseImpl(getProductUseCase, updateProductUseCase)
    val addProductUseCase = AddProductUseCaseImpl(
        testData.productRepository,
        getDefaultAislesUseCase,
        addAisleProductsUseCase,
        isProductNameUniqueUseCase
    )

    /**
     * Shopping List Use Cases
     */
    val getShoppingListUseCase = GetShoppingListUseCase(testData.locationRepository)

    /**
     * Backup/Restore Use Cases
     */
    val backupDatabaseUseCase = BackupDatabaseUseCaseImpl(testData.databaseMaintenance)
    val restoreDatabaseUseCase = RestoreDatabaseUseCaseImpl(testData.databaseMaintenance)

    /**
     * Location Use Cases with Dependencies
     */
    val removeLocationUseCase = RemoveLocationUseCaseImpl(
        testData.locationRepository,
        removeAisleUseCase,
        removeDefaultAisleUseCase
    )
    val addLocationUseCase = AddLocationUseCaseImpl(
        testData.locationRepository,
        addAisleUseCase,
        getAllProductsUseCase,
        addAisleProductsUseCase,
        isLocationNameUniqueUseCase
    )

    /**
     * Welcome Use Cases
     */
    val createSampleDataUseCase = CreateSampleDataUseCaseImpl(
        addProductUseCase = addProductUseCase,
        addAisleUseCase = addAisleUseCase,
        getShoppingListUseCase = getShoppingListUseCase,
        updateAisleProductRankUseCase = updateAisleProductRankUseCase,
        addLocationUseCase = addLocationUseCase,
        getAllProductsUseCase = getAllProductsUseCase,
        getHomeLocationUseCase = getHomeLocationUseCase
    )


}