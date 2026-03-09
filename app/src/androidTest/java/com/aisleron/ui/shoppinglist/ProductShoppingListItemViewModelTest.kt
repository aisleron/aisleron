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

package com.aisleron.ui.shoppinglist

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.copyentity.CopyEntityType
import com.aisleron.ui.note.NoteParentRef
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.collections.first
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductShoppingListItemViewModelTest : KoinTest {
    private val productRepository: ProductRepository by lazy { get<ProductRepository>() }

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private fun getProductShoppingListItemViewModel(
        headerRank: Int,
        aisleProduct: AisleProduct,
        locationId: Int
    ) = ProductShoppingListItemViewModel(
        headerRank = headerRank,
        selected = false,
        locationId = locationId,
        inStock = aisleProduct.product.inStock,
        qtyNeeded = aisleProduct.product.qtyNeeded,
        noteId = aisleProduct.product.noteId,
        noteText = aisleProduct.product.note?.noteText,
        qtyIncrement = aisleProduct.product.qtyIncrement,
        unitOfMeasure = aisleProduct.product.unitOfMeasure,
        trackingMode = aisleProduct.product.trackingMode,
        rank = aisleProduct.rank,
        id = aisleProduct.product.id,
        name = aisleProduct.product.name,
        aisleId = aisleProduct.aisleId,
        aisleProductId = aisleProduct.id
    ).apply {
        updateAisleProductRankUseCase = get<UpdateAisleProductRankUseCase>()
        removeProductUseCase = get<RemoveProductUseCase>()
        updateProductStatusUseCase = get<UpdateProductStatusUseCase>()
        updateProductQtyNeededUseCase = get<UpdateProductQtyNeededUseCase>()
        changeProductAisleUseCase = get<ChangeProductAisleUseCase>()
    }

    private suspend fun getShoppingList(): Location {
        val locationRepository = get<LocationRepository>()
        val locationId = locationRepository.getAll().first().id
        return locationRepository.getLocationWithAislesWithProducts(locationId).first()!!
    }

    @Test
    fun removeItem_ItemIsValidProduct_ProductRemoved() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.last()
        val shoppingListItem = getProductShoppingListItemViewModel(
            existingAisle.rank,
            aisleProduct,
            existingAisle.locationId
        )

        shoppingListItem.remove()

        val removedProduct = productRepository.get(aisleProduct.product.id)
        assertNull(removedProduct)
    }

    @Test
    fun removeItem_ItemIsInvalidProduct_NoProductRemoved() = runTest {
        val ap = AisleProduct(
            id = 1,
            aisleId = 1,
            product = Product(
                id = -1,
                name = "Dummy",
                inStock = false,
                qtyNeeded = 0.0,
                noteId = null,
                note = null,
                qtyIncrement = 1.0,
                trackingMode = TrackingMode.DEFAULT,
                unitOfMeasure = "Qty"
            ),
            rank = 1000
        )

        val shoppingListItem = getProductShoppingListItemViewModel(1000, ap, 1)
        val productCountBefore = productRepository.getAll().count()

        shoppingListItem.remove()

        val productCountAfter = productRepository.getAll().count()
        assertEquals(productCountBefore, productCountAfter)
    }

    @Test
    fun updateItemRank_ProductMovedInSameAisle_ProductRankUpdated() = runTest {
        val existingAisle = getShoppingList().aisles.first { it.products.count() > 1 }
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem =
            getProductShoppingListItemViewModel(
                existingAisle.rank,
                movedAisleProduct,
                existingAisle.locationId
            )

        val precedingAisleProduct = existingAisle.products.first { it.id != movedAisleProduct.id }
        val precedingItem =
            getProductShoppingListItemViewModel(
                existingAisle.rank,
                precedingAisleProduct,
                existingAisle.locationId
            )

        shoppingListItem.updateRank(precedingItem)

        val updatedAisleProduct = get<AisleProductRepository>().get(movedAisleProduct.id)
        assertEquals(precedingItem.rank + 1, updatedAisleProduct?.rank)
    }

    @Test
    fun updateItemRank_ProductMovedToDifferentAisle_ProductAisleUpdated() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem =
            getProductShoppingListItemViewModel(
                existingAisle.rank,
                movedAisleProduct,
                existingAisle.locationId
            )

        val targetAisle = get<AisleRepository>().getAll()
            .first { it.locationId == existingAisle.locationId && !it.isDefault && it.id != existingAisle.id }

        val precedingItem = AisleShoppingListItemViewModel(
            selected = false,
            childCount = targetAisle.products.count(),
            locationId = targetAisle.locationId,
            isDefault = targetAisle.isDefault,
            expanded = targetAisle.expanded,
            rank = targetAisle.rank,
            id = targetAisle.id,
            name = targetAisle.name
        ).apply {
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>()
            removeAisleUseCase = get<RemoveAisleUseCase>()
            updateAisleExpandedUseCase = get<UpdateAisleExpandedUseCase>()
        }

        shoppingListItem.updateRank(precedingItem)

        val updatedAisleProduct = get<AisleProductRepository>().get(movedAisleProduct.id)
        assertEquals(1, updatedAisleProduct?.rank)
        assertEquals(targetAisle.id, updatedAisleProduct?.aisleId)
    }

    @Test
    fun updateItemRank_NullPrecedingItem_ProductRankIsOne() = runTest {
        val existingAisle = getShoppingList().aisles.first { it.products.count() > 1 }
        val movedAisleProduct = existingAisle.products.last()
        val shoppingListItem =
            getProductShoppingListItemViewModel(
                existingAisle.rank,
                movedAisleProduct,
                existingAisle.locationId
            )

        shoppingListItem.updateRank(null)

        val updatedAisleProduct = get<AisleProductRepository>().get(movedAisleProduct.id)
        assertEquals(1, updatedAisleProduct?.rank)
        assertEquals(existingAisle.id, updatedAisleProduct?.aisleId)
    }

    private suspend fun updateStatus_ArrangeActAssert(newInStock: Boolean) {
        val initialItems = getShoppingList().aisles.flatMap { aisle ->
            aisle.products
                .filter { it.product.inStock != newInStock }
                .map { product -> aisle to product } // Creates a Pair<Aisle, AisleProduct>
        }

        val existingAisle = initialItems.first().first
        val aisleProduct = initialItems.first().second
        val shoppingListItem = getProductShoppingListItemViewModel(
            existingAisle.rank,
            aisleProduct,
            existingAisle.locationId
        )

        shoppingListItem.updateStatus(newInStock)

        val updatedProduct = productRepository.get(aisleProduct.product.id)
        assertEquals(newInStock, updatedProduct?.inStock)
    }

    @Test
    fun updateStatus_InStockTrue_ProductUpdatedToInStock() = runTest {
        updateStatus_ArrangeActAssert(true)
    }

    @Test
    fun updateStatus_InStockFalse_ProductUpdatedToNotInStock() = runTest {
        updateStatus_ArrangeActAssert(false)
    }

    @Test
    fun updateQtyNeeded_ValidQtyNeeded_ProductQtyNeededUpdated() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.first()
        val newQty = aisleProduct.product.qtyNeeded + 2.0
        val shoppingListItem = getProductShoppingListItemViewModel(
            existingAisle.rank,
            aisleProduct,
            existingAisle.locationId
        )

        shoppingListItem.updateQtyNeeded(newQty)

        val updatedProduct = productRepository.get(aisleProduct.product.id)
        assertEquals(newQty, updatedProduct?.qtyNeeded)
    }

    @Test
    fun updateQtyNeeded_QtyNeededIsNull_ProductQtyNeededNotUpdated() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.first()
        val newQty = null
        val shoppingListItem = getProductShoppingListItemViewModel(
            existingAisle.rank,
            aisleProduct,
            existingAisle.locationId
        )

        shoppingListItem.updateQtyNeeded(newQty)

        val updatedProduct = productRepository.get(aisleProduct.product.id)
        assertEquals(aisleProduct.product.qtyNeeded, updatedProduct?.qtyNeeded)
    }

    @Test
    fun updateAisle_ValidAisle_ProductAisleUpdated() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.last()
        val shoppingListItem =
            getProductShoppingListItemViewModel(
                existingAisle.rank,
                aisleProduct,
                existingAisle.locationId
            )

        val targetAisle = get<AisleRepository>().getAll()
            .first { it.locationId == existingAisle.locationId && !it.isDefault && it.id != existingAisle.id }

        shoppingListItem.updateAisle(targetAisle.id)

        val updatedAisleProduct = get<AisleProductRepository>().get(aisleProduct.id)
        assertEquals(targetAisle.id, updatedAisleProduct?.aisleId)
    }

    @Test
    fun navigateToEditEvent_ReturnsNavigateToEditProductEvent() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.first()
        val shoppingListItem = getProductShoppingListItemViewModel(
            existingAisle.rank,
            aisleProduct,
            existingAisle.locationId
        )

        val event = shoppingListItem.editNavigationEvent()

        assertEquals(
            ShoppingListViewModel.ShoppingListEvent.NavigateToEditProduct(aisleProduct.product.id),
            event
        )
    }

    @Test
    fun uniqueId_MatchesProductId() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.last()
        val expected = ShoppingListItem.UniqueId(
            ShoppingListItem.ItemType.PRODUCT, aisleProduct.id
        )

        val shoppingListItem =
            getProductShoppingListItemViewModel(
                existingAisle.rank,
                aisleProduct,
                existingAisle.locationId
            )

        assertEquals(expected, shoppingListItem.uniqueId)
    }

    @Test
    fun copyDialogNavigationEvent_ReturnsNavigateToCopyDialogEvent() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.first()
        val shoppingListItem = getProductShoppingListItemViewModel(
            existingAisle.rank,
            aisleProduct,
            existingAisle.locationId
        )

        val event = shoppingListItem.copyDialogNavigationEvent()

        val expected = ShoppingListViewModel.ShoppingListEvent.NavigateToCopyDialog(
            CopyEntityType.Product(aisleProduct.product.id), aisleProduct.product.name
        )

        assertEquals(expected, event)
    }

    @Test
    fun noteDialogNavigationEvent_ReturnsNavigateToNoteDialogEvent() = runTest {
        val existingAisle = getShoppingList().aisles.first()
        val aisleProduct = existingAisle.products.first()
        val shoppingListItem = getProductShoppingListItemViewModel(
            existingAisle.rank,
            aisleProduct,
            existingAisle.locationId
        )

        val event = shoppingListItem.noteDialogNavigationEvent()

        val expected = ShoppingListViewModel.ShoppingListEvent.NavigateToNoteDialog(
            NoteParentRef.Product(aisleProduct.product.id)
        )

        assertEquals(expected, event)
    }
}