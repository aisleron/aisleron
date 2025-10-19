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

import com.aisleron.domain.TransactionRunner
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
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardToLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardToLocationUseCaseImpl
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCaseImpl
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCaseImpl
import com.aisleron.domain.loyaltycard.usecase.RemoveLoyaltyCardFromLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.RemoveLoyaltyCardFromLocationUseCaseImpl
import com.aisleron.domain.note.usecase.AddNoteToParentUseCase
import com.aisleron.domain.note.usecase.AddNoteToParentUseCaseImpl
import com.aisleron.domain.note.usecase.AddNoteUseCase
import com.aisleron.domain.note.usecase.AddNoteUseCaseImpl
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCase
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCaseImpl
import com.aisleron.domain.note.usecase.CopyNoteUseCase
import com.aisleron.domain.note.usecase.CopyNoteUseCaseImpl
import com.aisleron.domain.note.usecase.GetNoteParentUseCase
import com.aisleron.domain.note.usecase.GetNoteParentUseCaseImpl
import com.aisleron.domain.note.usecase.GetNoteUseCase
import com.aisleron.domain.note.usecase.GetNoteUseCaseImpl
import com.aisleron.domain.note.usecase.RemoveNoteFromParentUseCase
import com.aisleron.domain.note.usecase.RemoveNoteFromParentUseCaseImpl
import com.aisleron.domain.note.usecase.RemoveNoteUseCase
import com.aisleron.domain.note.usecase.RemoveNoteUseCaseImpl
import com.aisleron.domain.note.usecase.UpdateNoteUseCase
import com.aisleron.domain.note.usecase.UpdateNoteUseCaseImpl
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.CopyProductUseCase
import com.aisleron.domain.product.usecase.CopyProductUseCaseImpl
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCaseImpl
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCaseImpl
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCaseImpl
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCaseImpl
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCaseImpl
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import com.aisleron.testdata.data.TransactionRunnerTestImpl

class TestUseCaseFactory(private val repositoryFactory: TestRepositoryFactory) {
    /**
     * Aisle Use Cases
     */
    val addAisleUseCase: AddAisleUseCase by lazy {
        AddAisleUseCaseImpl(
            repositoryFactory.aisleRepository,
            getLocationUseCase = getLocationUseCase,
            isAisleNameUniqueUseCase = isAisleNameUniqueUseCase
        )
    }

    val getAisleUseCase: GetAisleUseCase by lazy {
        GetAisleUseCaseImpl(repositoryFactory.aisleRepository)
    }

    val getDefaultAislesUseCase: GetDefaultAislesUseCase by lazy {
        GetDefaultAislesUseCase(repositoryFactory.aisleRepository)
    }

    val isAisleNameUniqueUseCase: IsAisleNameUniqueUseCase by lazy {
        IsAisleNameUniqueUseCase(repositoryFactory.aisleRepository)
    }

    val removeAisleUseCase: RemoveAisleUseCase by lazy {
        RemoveAisleUseCaseImpl(
            repositoryFactory.aisleRepository,
            updateAisleProductsUseCase = updateAisleProductsUseCase,
            removeProductsFromAisleUseCase = removeProductsFromAisleUseCase
        )
    }

    val removeDefaultAisleUseCase: RemoveDefaultAisleUseCase by lazy {
        RemoveDefaultAisleUseCase(
            repositoryFactory.aisleRepository,
            removeProductsFromAisleUseCase = removeProductsFromAisleUseCase
        )
    }

    val updateAisleExpandedUseCase: UpdateAisleExpandedUseCase by lazy {
        UpdateAisleExpandedUseCaseImpl(
            getAisleUseCase = getAisleUseCase,
            updateAisleUseCase = updateAisleUseCase
        )
    }

    val updateAisleRankUseCase: UpdateAisleRankUseCase by lazy {
        UpdateAisleRankUseCase(repositoryFactory.aisleRepository)
    }

    val updateAisleUseCase: UpdateAisleUseCase by lazy {
        UpdateAisleUseCaseImpl(
            repositoryFactory.aisleRepository,
            getLocationUseCase = getLocationUseCase,
            isAisleNameUniqueUseCase = isAisleNameUniqueUseCase
        )
    }

    /**
     * Aisle Product Use Cases
     */
    val addAisleProductUseCase: AddAisleProductsUseCase by lazy {
        AddAisleProductsUseCase(repositoryFactory.aisleProductRepository)
    }

    val getAisleMaxRankUseCase: GetAisleMaxRankUseCase by lazy {
        GetAisleMaxRankUseCase(repositoryFactory.aisleProductRepository)
    }

    val removeProductsFromAisleUseCase: RemoveProductsFromAisleUseCase by lazy {
        RemoveProductsFromAisleUseCase(repositoryFactory.aisleProductRepository)
    }

    val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase by lazy {
        UpdateAisleProductRankUseCase(repositoryFactory.aisleProductRepository)
    }

    val updateAisleProductsUseCase: UpdateAisleProductsUseCase by lazy {
        UpdateAisleProductsUseCase(repositoryFactory.aisleProductRepository)
    }

    /**
     * Location Use Cases
     */
    val addLocationUseCase: AddLocationUseCase by lazy {
        AddLocationUseCaseImpl(
            repositoryFactory.locationRepository,
            addAisleUseCase = addAisleUseCase,
            getAllProductsUseCase = getAllProductsUseCase,
            addAisleProductsUseCase = addAisleProductUseCase,
            isLocationNameUniqueUseCase = isLocationNameUniqueUseCase
        )
    }

    val copyLocationUseCase: CopyLocationUseCase by lazy {
        CopyLocationUseCaseImpl(
            repositoryFactory.locationRepository,
            repositoryFactory.aisleRepository,
            repositoryFactory.aisleProductRepository,
            isLocationNameUniqueUseCase = isLocationNameUniqueUseCase
        )
    }

    val getHomeLocationUseCase: GetHomeLocationUseCase by lazy {
        GetHomeLocationUseCase(repositoryFactory.locationRepository)
    }

    val getLocationUseCase: GetLocationUseCase by lazy {
        GetLocationUseCase(repositoryFactory.locationRepository)
    }

    val isLocationNameUniqueUseCase: IsLocationNameUniqueUseCase by lazy {
        IsLocationNameUniqueUseCase(repositoryFactory.locationRepository)
    }

    val getPinnedShopsUseCase: GetPinnedShopsUseCase by lazy {
        GetPinnedShopsUseCase(repositoryFactory.locationRepository)
    }

    val getShopsUseCase: GetShopsUseCase by lazy {
        GetShopsUseCase(repositoryFactory.locationRepository)
    }

    val removeLocationUseCase: RemoveLocationUseCase by lazy {
        RemoveLocationUseCaseImpl(
            repositoryFactory.locationRepository,
            removeAisleUseCase = removeAisleUseCase,
            removeDefaultAisleUseCase = removeDefaultAisleUseCase
        )
    }

    val updateLocationUseCase: UpdateLocationUseCase by lazy {
        UpdateLocationUseCase(
            repositoryFactory.locationRepository,
            isLocationNameUniqueUseCase = isLocationNameUniqueUseCase
        )
    }

    /**
     * Loyalty Card Use Cases
     */
    val addLoyaltyCardToLocationUseCase: AddLoyaltyCardToLocationUseCase by lazy {
        AddLoyaltyCardToLocationUseCaseImpl(
            repositoryFactory.loyaltyCardRepository,
            getLocationUseCase = getLocationUseCase
        )
    }

    val addLoyaltyCardUseCase: AddLoyaltyCardUseCase by lazy {
        AddLoyaltyCardUseCaseImpl(repositoryFactory.loyaltyCardRepository)
    }

    val getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase by lazy {
        GetLoyaltyCardForLocationUseCaseImpl(repositoryFactory.loyaltyCardRepository)
    }

    val removeLoyaltyCardFromLocationUseCase: RemoveLoyaltyCardFromLocationUseCase by lazy {
        RemoveLoyaltyCardFromLocationUseCaseImpl(repositoryFactory.loyaltyCardRepository)
    }

    /**
     * Note Use Cases
     */
    val addNoteUseCase: AddNoteUseCase by lazy {
        AddNoteUseCaseImpl(
            repositoryFactory.noteRepository,
            addNoteToParentUseCase = addNoteToParentUseCase,
            transactionRunner = transactionRunner
        )
    }

    val applyNoteChangesUseCase: ApplyNoteChangesUseCase by lazy {
        ApplyNoteChangesUseCaseImpl(
            addNoteUseCase = addNoteUseCase,
            updateNoteUseCase = updateNoteUseCase,
            removeNoteUseCase = removeNoteUseCase,
        )
    }

    val copyNoteUseCase: CopyNoteUseCase by lazy {
        CopyNoteUseCaseImpl(addNoteUseCase, getNoteUseCase)
    }

    val getNoteParentUseCase: GetNoteParentUseCase by lazy {
        GetNoteParentUseCaseImpl(
            getProductUseCase = getProductUseCase,
            getNoteUseCase = getNoteUseCase
        )
    }

    val getNoteUseCase: GetNoteUseCase by lazy {
        GetNoteUseCaseImpl(repositoryFactory.noteRepository)
    }

    val addNoteToParentUseCase: AddNoteToParentUseCase by lazy {
        AddNoteToParentUseCaseImpl(removeNoteUseCase, updateProductUseCase)
    }

    val updateNoteUseCase: UpdateNoteUseCase by lazy {
        UpdateNoteUseCaseImpl(repositoryFactory.noteRepository)
    }

    val removeNoteFromParentUseCase: RemoveNoteFromParentUseCase by lazy {
        RemoveNoteFromParentUseCaseImpl(updateProductUseCase)
    }

    val removeNoteUseCase: RemoveNoteUseCase by lazy {
        RemoveNoteUseCaseImpl(
            repositoryFactory.noteRepository,
            removeNoteFromParentUseCase = removeNoteFromParentUseCase,
            transactionRunner = transactionRunner
        )
    }

    /**
     * Product Use Cases
     */
    val addProductUseCase: AddProductUseCase by lazy {
        AddProductUseCaseImpl(
            productRepository = repositoryFactory.productRepository,
            getDefaultAislesUseCase = getDefaultAislesUseCase,
            addAisleProductsUseCase = addAisleProductUseCase,
            isProductNameUniqueUseCase = isProductNameUniqueUseCase,
            getAisleMaxRankUseCase = getAisleMaxRankUseCase,
            transactionRunner = transactionRunner
        )
    }

    val copyProductUseCase: CopyProductUseCase by lazy {
        CopyProductUseCaseImpl(
            repositoryFactory.productRepository,
            repositoryFactory.aisleProductRepository,
            isProductNameUniqueUseCase = isProductNameUniqueUseCase,
            copyNoteUseCase = copyNoteUseCase,
            transactionRunner = transactionRunner
        )
    }

    val getAllProductsUseCase: GetAllProductsUseCase by lazy {
        GetAllProductsUseCase(repositoryFactory.productRepository)
    }

    val getProductUseCase: GetProductUseCase by lazy {
        GetProductUseCaseImpl(
            repositoryFactory.productRepository,
            getNoteUseCase = getNoteUseCase
        )
    }

    val isProductNameUniqueUseCase: IsProductNameUniqueUseCase by lazy {
        IsProductNameUniqueUseCase(repositoryFactory.productRepository)
    }

    val removeProductUseCase: RemoveProductUseCase by lazy {
        RemoveProductUseCaseImpl(
            repositoryFactory.productRepository,
            removeNoteUseCase = removeNoteUseCase,
            transactionRunner = transactionRunner
        )
    }

    val updateProductStatusUseCase: UpdateProductStatusUseCase by lazy {
        UpdateProductStatusUseCaseImpl(
            getProductUseCase = getProductUseCase,
            updateProductUseCase = updateProductUseCase
        )
    }

    val updateProductUseCase: UpdateProductUseCase by lazy {
        UpdateProductUseCaseImpl(
            productRepository = repositoryFactory.productRepository,
            isProductNameUniqueUseCase = isProductNameUniqueUseCase
        )
    }

    /**
     * Sample Data Use Case
     */
    val createSampleDataUseCase: CreateSampleDataUseCase by lazy {
        CreateSampleDataUseCaseImpl(
            addProductUseCase = addProductUseCase,
            addAisleUseCase = addAisleUseCase,
            getShoppingListUseCase = getShoppingListUseCase,
            updateAisleProductRankUseCase = updateAisleProductRankUseCase,
            addLocationUseCase = addLocationUseCase,
            getAllProductsUseCase = getAllProductsUseCase,
            getHomeLocationUseCase = getHomeLocationUseCase
        )
    }

    /**
     * Shopping List Use Cases
     */
    val getShoppingListUseCase: GetShoppingListUseCase by lazy {
        GetShoppingListUseCase(repositoryFactory.locationRepository)
    }

    /**
     * Other
     */
    private val transactionRunner: TransactionRunner by lazy {
        TransactionRunnerTestImpl()
    }

    inline fun <reified T> get(): T {
        return when (T::class) {
            // Aisle Use Cases
            UpdateAisleExpandedUseCase::class -> updateAisleExpandedUseCase as T
            AddAisleUseCase::class -> addAisleUseCase as T
            GetAisleMaxRankUseCase::class -> getAisleMaxRankUseCase as T
            GetAisleUseCase::class -> getAisleUseCase as T
            GetDefaultAislesUseCase::class -> getDefaultAislesUseCase as T
            IsAisleNameUniqueUseCase::class -> isAisleNameUniqueUseCase as T
            RemoveAisleUseCase::class -> removeAisleUseCase as T
            RemoveDefaultAisleUseCase::class -> removeDefaultAisleUseCase as T
            UpdateAisleRankUseCase::class -> updateAisleRankUseCase as T
            UpdateAisleUseCase::class -> updateAisleUseCase as T

            // AisleProduct Use Cases
            AddAisleProductsUseCase::class -> addAisleProductUseCase as T
            RemoveProductsFromAisleUseCase::class -> removeProductsFromAisleUseCase as T
            UpdateAisleProductsUseCase::class -> updateAisleProductsUseCase as T
            UpdateAisleProductRankUseCase::class -> updateAisleProductRankUseCase as T

            // Location Use Cases
            AddLocationUseCase::class -> addLocationUseCase as T
            CopyLocationUseCase::class -> copyLocationUseCase as T
            GetHomeLocationUseCase::class -> getHomeLocationUseCase as T
            GetLocationUseCase::class -> getLocationUseCase as T
            IsLocationNameUniqueUseCase::class -> isLocationNameUniqueUseCase as T
            GetPinnedShopsUseCase::class -> getPinnedShopsUseCase as T
            GetShopsUseCase::class -> getShopsUseCase as T
            RemoveLocationUseCase::class -> removeLocationUseCase as T
            UpdateLocationUseCase::class -> updateLocationUseCase as T

            //Loyalty Card Use Cases
            AddLoyaltyCardToLocationUseCase::class -> addLoyaltyCardToLocationUseCase as T
            AddLoyaltyCardUseCase::class -> addLoyaltyCardUseCase as T
            GetLoyaltyCardForLocationUseCase::class -> getLoyaltyCardForLocationUseCase as T
            RemoveLoyaltyCardFromLocationUseCase::class -> removeLoyaltyCardFromLocationUseCase as T

            // Note Use Cases
            AddNoteUseCase::class -> addNoteUseCase as T
            ApplyNoteChangesUseCase::class -> applyNoteChangesUseCase as T
            CopyNoteUseCase::class -> copyNoteUseCase as T
            GetNoteParentUseCase::class -> getNoteParentUseCase as T
            GetNoteUseCase::class -> getNoteUseCase as T
            AddNoteToParentUseCase::class -> addNoteToParentUseCase as T
            UpdateNoteUseCase::class -> updateNoteUseCase as T
            RemoveNoteFromParentUseCase::class -> removeNoteFromParentUseCase as T
            RemoveNoteUseCase::class -> removeNoteUseCase as T

            // Product Use Cases
            AddProductUseCase::class -> addProductUseCase as T
            CopyProductUseCase::class -> copyProductUseCase as T
            GetAllProductsUseCase::class -> getAllProductsUseCase as T
            GetProductUseCase::class -> getProductUseCase as T
            IsProductNameUniqueUseCase::class -> isProductNameUniqueUseCase as T
            RemoveProductUseCase::class -> removeProductUseCase as T
            UpdateProductStatusUseCase::class -> updateProductStatusUseCase as T
            UpdateProductUseCase::class -> updateProductUseCase as T

            // Create Sample Data Use Case
            CreateSampleDataUseCase::class -> createSampleDataUseCase as T

            // Shopping List Use Cases
            GetShoppingListUseCase::class -> getShoppingListUseCase as T

            else -> throw IllegalArgumentException("Unknown use case ${T::class}")
        }
    }
}