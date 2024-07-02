package com.aisleron.ui.shoppinglist

import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.location.Location
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ProductShoppingListItemViewModelTest {
    private lateinit var testData: TestDataManager
    private lateinit var testUseCases: TestUseCaseProvider

    @Before
    fun setUp() {
        testData = TestDataManager()
        testUseCases = TestUseCaseProvider(testData)
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
        aisleId = aisleProduct.aisleId,
        aisleProductId = aisleProduct.id,
        updateAisleProductRankUseCase = testUseCases.updateAisleProductRankUseCase,
        removeProductUseCase = testUseCases.removeProductUseCase
    )

    private fun getShoppingList(): Location {
        return runBlocking {
            val locationId = testData.locationRepository.getAll().first().id
            testData.locationRepository.getLocationWithAislesWithProducts(locationId)
                .first()!!
        }
    }

    @Test
    fun removeItem_ItemIsValidProduct_ProductRemoved() {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(existingAisle, aisleProduct)

        runBlocking { shoppingListItem.remove() }

        val removedProduct = runBlocking { testData.productRepository.get(aisleProduct.product.id) }
        Assert.assertNull(removedProduct)
    }

    @Test
    fun removeItem_ItemIsInvalidProduct_NoProductRemoved() {
        val shoppingListItem = ProductShoppingListItemViewModel(
            aisleRank = 1000,
            rank = 1000,
            id = -1,
            name = "Dummy",
            inStock = false,
            aisleId = 1,
            aisleProductId = 1,
            updateAisleProductRankUseCase = testUseCases.updateAisleProductRankUseCase,
            removeProductUseCase = testUseCases.removeProductUseCase
        )

        val productCountBefore = runBlocking { testData.productRepository.getAll().count() }
        runBlocking { shoppingListItem.remove() }
        val productCountAfter = runBlocking { testData.productRepository.getAll().count() }

        Assert.assertEquals(productCountBefore, productCountAfter)
    }

    @Test
    fun updateItemRank_ProductMovedInSameAisle_ProductRankUpdated() {
        val existingAisle = getShoppingList().aisles.first { it.products.count() > 1 }
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(existingAisle, movedAisleProduct)

        val precedingAisleProduct = existingAisle.products.first { it.id != movedAisleProduct.id }
        val precedingItem =
            getProductShoppingListItemViewModel(existingAisle, precedingAisleProduct)

        runBlocking { shoppingListItem.updateRank(precedingItem) }

        val updatedAisleProduct =
            runBlocking { testData.aisleProductRepository.get(movedAisleProduct.id) }
        Assert.assertEquals(precedingItem.rank + 1, updatedAisleProduct?.rank)
    }

    @Test
    fun updateItemRank_ProductMovedToDifferentAisle_ProductAisleUpdated() {
        val existingAisle = getShoppingList().aisles.first()
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(existingAisle, movedAisleProduct)

        val targetAisle = runBlocking {
            testData.aisleRepository.getAll()
                .first { it.locationId == existingAisle.locationId && !it.isDefault && it.id != existingAisle.id }
        }

        val precedingItem = AisleShoppingListItemViewModel(
            rank = targetAisle.rank,
            id = targetAisle.id,
            name = targetAisle.name,
            isDefault = targetAisle.isDefault,
            childCount = 0,
            locationId = targetAisle.locationId,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        runBlocking { shoppingListItem.updateRank(precedingItem) }

        val updatedAisleProduct =
            runBlocking { testData.aisleProductRepository.get(movedAisleProduct.id) }
        Assert.assertEquals(1, updatedAisleProduct?.rank)
        Assert.assertEquals(targetAisle.id, updatedAisleProduct?.aisleId)
    }

    @Test
    fun updateItemRank_NullPrecedingItem_ProductRankIsOne() {
        val existingAisle = getShoppingList().aisles.first { it.products.count() > 1 }
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(existingAisle, movedAisleProduct)

        runBlocking { shoppingListItem.updateRank(null) }

        val updatedAisleProduct =
            runBlocking { testData.aisleProductRepository.get(movedAisleProduct.id) }

        Assert.assertEquals(1, updatedAisleProduct?.rank)
        Assert.assertEquals(existingAisle.id, updatedAisleProduct?.aisleId)
    }
}