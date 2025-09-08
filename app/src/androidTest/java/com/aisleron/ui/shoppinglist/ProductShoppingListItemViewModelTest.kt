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

package com.aisleron.ui.shoppinglist

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get

class ProductShoppingListItemViewModelTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private fun getProductShoppingListItemViewModel(
        aisle: Aisle,
        aisleProduct: AisleProduct
    ) = ProductShoppingListItemViewModel(
        aisleRank = aisle.rank,
        rank = aisleProduct.rank,
        id = aisleProduct.product.id,
        name = aisleProduct.product.name,
        inStock = aisleProduct.product.inStock,
        qtyNeeded = aisleProduct.product.qtyNeeded,
        aisleId = aisleProduct.aisleId,
        aisleProductId = aisleProduct.id,
        updateAisleProductRankUseCase = get<UpdateAisleProductRankUseCase>(),
        removeProductUseCase = get<RemoveProductUseCase>()
    )

    private fun getShoppingList(): Location {
        return runBlocking {
            val locationRepository = get<LocationRepository>()
            val locationId = locationRepository.getAll().first().id
            locationRepository.getLocationWithAislesWithProducts(locationId).first()!!
        }
    }

    @Test
    fun removeItem_ItemIsValidProduct_ProductRemoved() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(existingAisle, aisleProduct)

        shoppingListItem.remove()

        val removedProduct = get<ProductRepository>().get(aisleProduct.product.id)
        Assert.assertNull(removedProduct)
    }

    @Test
    fun removeItem_ItemIsInvalidProduct_NoProductRemoved() = runTest {
        val shoppingListItem = ProductShoppingListItemViewModel(
            aisleRank = 1000,
            rank = 1000,
            id = -1,
            name = "Dummy",
            inStock = false,
            qtyNeeded = 0,
            aisleId = 1,
            aisleProductId = 1,
            updateAisleProductRankUseCase = get<UpdateAisleProductRankUseCase>(),
            removeProductUseCase = get<RemoveProductUseCase>()
        )

        val productRepository = get<ProductRepository>()
        val productCountBefore = productRepository.getAll().count()
        runBlocking { shoppingListItem.remove() }
        val productCountAfter = productRepository.getAll().count()

        Assert.assertEquals(productCountBefore, productCountAfter)
    }

    @Test
    fun updateItemRank_ProductMovedInSameAisle_ProductRankUpdated() = runTest {
        val existingAisle = getShoppingList().aisles.first { it.products.count() > 1 }
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(existingAisle, movedAisleProduct)

        val precedingAisleProduct = existingAisle.products.first { it.id != movedAisleProduct.id }
        val precedingItem =
            getProductShoppingListItemViewModel(existingAisle, precedingAisleProduct)

        runBlocking { shoppingListItem.updateRank(precedingItem) }

        val updatedAisleProduct = get<AisleProductRepository>().get(movedAisleProduct.id)
        Assert.assertEquals(precedingItem.rank + 1, updatedAisleProduct?.rank)
    }

    @Test
    fun updateItemRank_ProductMovedToDifferentAisle_ProductAisleUpdated() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(existingAisle, movedAisleProduct)

        val targetAisle = get<AisleRepository>().getAll()
            .first { it.locationId == existingAisle.locationId && !it.isDefault && it.id != existingAisle.id }

        val precedingItem = AisleShoppingListItemViewModel(
            rank = targetAisle.rank,
            id = targetAisle.id,
            name = targetAisle.name,
            isDefault = targetAisle.isDefault,
            childCount = 0,
            locationId = targetAisle.locationId,
            expanded = targetAisle.expanded,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        shoppingListItem.updateRank(precedingItem)

        val updatedAisleProduct = get<AisleProductRepository>().get(movedAisleProduct.id)
        Assert.assertEquals(1, updatedAisleProduct?.rank)
        Assert.assertEquals(targetAisle.id, updatedAisleProduct?.aisleId)
    }

    @Test
    fun updateItemRank_NullPrecedingItem_ProductRankIsOne() = runTest {
        val existingAisle = getShoppingList().aisles.first { it.products.count() > 1 }
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(existingAisle, movedAisleProduct)

        shoppingListItem.updateRank(null)

        val updatedAisleProduct = get<AisleProductRepository>().get(movedAisleProduct.id)

        Assert.assertEquals(1, updatedAisleProduct?.rank)
        Assert.assertEquals(existingAisle.id, updatedAisleProduct?.aisleId)
    }
}