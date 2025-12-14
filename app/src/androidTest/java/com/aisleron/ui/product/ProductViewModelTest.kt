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

package com.aisleron.ui.product

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAislesForLocationUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCase
import com.aisleron.domain.note.usecase.GetNoteParentUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductMappingsUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.bundles.AisleListEntry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductViewModelTest() : KoinTest {
    private lateinit var productViewModel: ProductViewModel
    private lateinit var productRepository: ProductRepository

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        productViewModel = get<ProductViewModel>()
        productRepository = get<ProductRepository>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private suspend fun testSaveProduct_ProductExists_UpdateProduct(inStock: Boolean) {
        val updatedProductName = "Updated Product Name"
        val existingProduct: Product = productRepository.getAll().first()
        val countBefore: Int = productRepository.getAll().count()

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.updateProductName(updatedProductName)
        productViewModel.updateInStock(inStock)
        productViewModel.saveProduct()

        val updatedProduct = productRepository.get(existingProduct.id)
        Assert.assertNotNull(updatedProduct)
        Assert.assertEquals(updatedProductName, updatedProduct?.name)
        Assert.assertEquals(inStock, updatedProduct?.inStock)

        val countAfter: Int = productRepository.getAll().count()
        Assert.assertEquals(countBefore, countAfter)
    }

    @Test
    fun testSaveProduct_ProductExistsAndInStockTrue_UpdateProduct() = runTest {
        testSaveProduct_ProductExists_UpdateProduct(true)
    }

    @Test
    fun testSaveProduct_ProductExistsAndInStockFalse_UpdateProduct() = runTest {
        testSaveProduct_ProductExists_UpdateProduct(false)
    }

    private suspend fun testSaveProduct_ProductDoesNotExists_CreateProduct(inStock: Boolean) {
        val newProductName = "New Product Name"
        val countBefore: Int = productRepository.getAll().count()

        productViewModel.hydrate(0, inStock)
        productViewModel.updateProductName(newProductName)
        productViewModel.updateInStock(inStock)
        productViewModel.saveProduct()

        val newProduct = productRepository.getByName(newProductName)
        Assert.assertNotNull(newProduct)
        Assert.assertEquals(newProductName, newProduct?.name)
        Assert.assertEquals(inStock, newProduct?.inStock)
        Assert.assertEquals(newProductName, productViewModel.uiData.value.productName)
        Assert.assertEquals(inStock, productViewModel.uiData.value.inStock)

        val countAfter: Int = productRepository.getAll().count()
        Assert.assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun testSaveProduct_ProductDoesNotExistAndInStockTrue_CreateProduct() = runTest {
        testSaveProduct_ProductDoesNotExists_CreateProduct(true)
    }

    @Test
    fun testSaveProduct_ProductDoesNotExistAndInStockFalse_CreateProduct() = runTest {
        testSaveProduct_ProductDoesNotExists_CreateProduct(false)
    }

    @Test
    fun testSaveProduct_SaveSuccessful_UiStateIsSuccess() = runTest {
        val updatedProductName = "Updated Product Name"
        val existingProduct: Product = productRepository.getAll().first()

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.updateProductName(updatedProductName)
        productViewModel.updateInStock(existingProduct.inStock)
        productViewModel.saveProduct()

        Assert.assertEquals(
            ProductViewModel.ProductUiState.Success, productViewModel.productUiState.value
        )
    }

    @Test
    fun testSaveProduct_ProductNameIsBlank_NoAction() = runTest {
        val updatedProductName = ""

        productViewModel.hydrate(0, true)
        productViewModel.updateProductName(updatedProductName)
        productViewModel.saveProduct()

        Assert.assertEquals(
            ProductViewModel.ProductUiState.Empty, productViewModel.productUiState.value
        )
    }

    @Test
    fun testSaveProduct_AisleronErrorOnSave_UiStateIsError() = runTest {
        val existingProduct: Product = productRepository.getAll().first()

        productViewModel.hydrate(0, existingProduct.inStock)
        productViewModel.updateProductName(existingProduct.name)
        productViewModel.updateInStock(!existingProduct.inStock)
        productViewModel.saveProduct()

        Assert.assertTrue(productViewModel.productUiState.value is ProductViewModel.ProductUiState.Error)
    }

    @Test
    fun testSaveProduct_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on save Product"

        declare<AddProductUseCase> {
            object : AddProductUseCase {
                override suspend fun invoke(item: Product): Int {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val pvm = get<ProductViewModel>()

        pvm.hydrate(0, false)
        pvm.updateProductName("Bogus Product")
        pvm.updateInStock(true)
        pvm.saveProduct()

        Assert.assertTrue(pvm.productUiState.value is ProductViewModel.ProductUiState.Error)
        Assert.assertEquals(
            AisleronException.ExceptionCode.GENERIC_EXCEPTION,
            (pvm.productUiState.value as ProductViewModel.ProductUiState.Error).errorCode
        )
        Assert.assertEquals(
            exceptionMessage,
            (pvm.productUiState.value as ProductViewModel.ProductUiState.Error).errorMessage
        )
    }

    @Test
    fun testGetProductName_ProductExists_ReturnsProductName() = runTest {
        val existingProduct: Product = productRepository.getAll().first()
        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        Assert.assertEquals(existingProduct.name, productViewModel.uiData.value.productName)
    }

    @Test
    fun testGetProductName_ProductDoesNotExists_ReturnsEmptyProductName() = runTest {
        productViewModel.hydrate(0, false)
        Assert.assertEquals("", productViewModel.uiData.value.productName)
    }

    @Test
    fun testHydrate_ProductDoesNotExists_UiStateIsEmpty() = runTest {
        productViewModel.hydrate(1, true)
        Assert.assertTrue(productViewModel.productUiState.value is ProductViewModel.ProductUiState.Empty)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ProductViewModelReturned() {
        val pvm = ProductViewModel(
            get<AddProductUseCase>(),
            get<UpdateProductUseCase>(),
            get<GetAisleUseCase>(),
            get<GetNoteParentUseCase>(),
            get<ApplyNoteChangesUseCase>(),
            get<GetProductMappingsUseCase>(),
            get<GetAislesForLocationUseCase>(),
            get<ChangeProductAisleUseCase>()
        )

        Assert.assertNotNull(pvm)
    }

    @Test
    fun testSaveProduct_AisleProvided_ProductAddedToAisle() = runTest {
        val newProductName = "New Product Name"
        val aisleProductRepository = get<AisleProductRepository>()
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        val inStock = true

        productViewModel.hydrate(0, inStock, aisle.id)
        val countBefore = aisleProductRepository.getAll().count { it.aisleId == aisle.id }
        productViewModel.updateProductName(newProductName)
        productViewModel.updateInStock(inStock)
        productViewModel.saveProduct()

        Assert.assertEquals(newProductName, productViewModel.uiData.value.productName)
        Assert.assertEquals(inStock, productViewModel.uiData.value.inStock)

        val newProduct = productRepository.getByName(newProductName)
        Assert.assertEquals(newProductName, newProduct?.name)
        Assert.assertEquals(inStock, newProduct?.inStock)

        val countAfter = aisleProductRepository.getAll().count { it.aisleId == aisle.id }
        Assert.assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun hydrate_ProductHasNote_NoteReturned() = runTest {
        val noteText = "Test note on product"
        val noteId = get<NoteRepository>().add(Note(0, noteText))
        val existingProduct: Product = productRepository.getAll().first()
        productRepository.update(existingProduct.copy(noteId = noteId))

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)

        Assert.assertEquals(noteText, productViewModel.noteFlow.value)
    }

    @Test
    fun updateNote_NewNoteProvided_NoteValueUpdated() = runTest {
        val noteText = "Test note on product"
        val noteId = get<NoteRepository>().add(Note(0, noteText))
        val existingProduct: Product = productRepository.getAll().first()
        productRepository.update(existingProduct.copy(noteId = noteId))
        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)

        val updatedNote = "Updated note text"
        productViewModel.updateNote(updatedNote)

        Assert.assertEquals(updatedNote, productViewModel.noteFlow.value)
    }

    @Test
    fun saveProduct_NoteIsBlankString_NoNoteCreated() = runTest {
        val existingProduct = productRepository.getAll().first()
        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.updateProductName("${existingProduct.name} Updated")

        productViewModel.saveProduct()

        val updatedProduct = productRepository.get(existingProduct.id)!!
        assertNull(updatedProduct.noteId)

        val noteCount = get<NoteRepository>().getAll().count()
        assertEquals(0, noteCount)
    }

    @Test
    fun saveProduct_ProductHasNoNote_NoteCreated() = runTest {
        val existingProduct = productRepository.getAll().first()
        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.updateProductName("${existingProduct.name} Updated")
        val noteText = "New note created"
        productViewModel.updateNote(noteText)

        productViewModel.saveProduct()

        val updatedProduct = productRepository.get(existingProduct.id)!!
        assertNotNull(updatedProduct.noteId)

        val note = get<NoteRepository>().get(updatedProduct.noteId)
        assertEquals(noteText, note?.noteText)
    }

    @Test
    fun saveProduct_ProductHasNote_NoteUpdated() = runTest {
        val noteId = get<NoteRepository>().add(Note(0, "Test note on product"))
        val existingProduct: Product = productRepository.getAll().first()
        productRepository.update(existingProduct.copy(noteId = noteId))
        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        val noteText = "New note created"
        productViewModel.updateNote(noteText)

        productViewModel.saveProduct()

        val updatedProduct = productRepository.get(existingProduct.id)!!
        assertEquals(noteId, updatedProduct.noteId)

        val note = get<NoteRepository>().get(updatedProduct.noteId!!)
        assertEquals(noteText, note?.noteText)
    }

    @Test
    fun requestLocationAisles_itemProvided_aislesForLocationFlowIsUpdated() = runTest {
        val product = productRepository.getAll().first()
        productViewModel.hydrate(product.id, product.inStock)
        val productAisleInfo = productViewModel.productAisles.value.first()

        productViewModel.requestLocationAisles(productAisleInfo)

        val aislesForLocation = productViewModel.aislesForLocation.value
        assertNotNull(aislesForLocation)
        assertEquals(productAisleInfo.locationName, aislesForLocation.title)
        assertEquals(productAisleInfo.aisleId, aislesForLocation.currentAisleId)
        val locationAisles = get<AisleRepository>().getForLocation(productAisleInfo.locationId)
            .sortedBy { it.rank }
            .map { AisleListEntry(it.id, it.name) }
        assertEquals(locationAisles, aislesForLocation.aisles)
    }

    @Test
    fun onAislesDialogShown_aislesForLocationIsSet_aislesForLocationSetToNull() = runTest {
        val product = productRepository.getAll().first()
        productViewModel.hydrate(product.id, product.inStock)
        val productAisleInfo = productViewModel.productAisles.value.first()
        productViewModel.requestLocationAisles(productAisleInfo)
        assertNotNull(productViewModel.aislesForLocation.value)

        productViewModel.onAislesDialogShown()

        assertNull(productViewModel.aislesForLocation.value)
    }

    @Test
    fun updateProductAisle_newAisleSelected_productAislesFlowIsUpdated() = runTest {
        val product = productRepository.getAll().first()
        productViewModel.hydrate(product.id, product.inStock)
        val aisleInfoToUpdate = productViewModel.productAisles.value.first { it.aisleId != 0 }
        productViewModel.requestLocationAisles(aisleInfoToUpdate)
        val newAisle = get<AisleRepository>().getForLocation(aisleInfoToUpdate.locationId)
            .first { it.id != aisleInfoToUpdate.aisleId }

        productViewModel.updateProductAisle(newAisle.id)

        val updatedAisleInfo =
            productViewModel.productAisles.value.first { it.locationId == aisleInfoToUpdate.locationId }

        assertEquals(newAisle.id, updatedAisleInfo.aisleId)
        assertEquals(newAisle.name, updatedAisleInfo.aisleName)
        assertEquals(aisleInfoToUpdate.initialAisleId, updatedAisleInfo.initialAisleId)
    }

    @Test
    fun saveProduct_aisleChanged_productAisleIsUpdatedInRepo() = runTest {
        val product = productRepository.getAll().first()
        productViewModel.hydrate(product.id, product.inStock)
        val aisleInfoToChange = productViewModel.productAisles.value.first { it.aisleId != 0 }
        val originalAisleId = aisleInfoToChange.initialAisleId
        productViewModel.requestLocationAisles(aisleInfoToChange)
        val newAisle = get<AisleRepository>().getForLocation(aisleInfoToChange.locationId)
            .first { it.id != aisleInfoToChange.aisleId }
        productViewModel.updateProductAisle(newAisle.id)
        val updatedAisleInfo =
            productViewModel.productAisles.value.first { it.locationId == aisleInfoToChange.locationId }

        assertNotEquals(updatedAisleInfo.initialAisleId, updatedAisleInfo.aisleId)

        productViewModel.saveProduct()

        val aisleProductRepository = get<AisleProductRepository>()
        val productAisles = aisleProductRepository.getProductAisles(product.id)
        assertNull(productAisles.find { it.aisleId == originalAisleId })
        assertNotNull(productAisles.find { it.aisleId == newAisle.id })
    }

    @Test
    fun requestProductAisles_NewLocationsAdded_NewLocationsIncludedInList() = runTest {
        val product = productRepository.getAll().first()
        productViewModel.hydrate(product.id, product.inStock)
        val initialAisles = productViewModel.productAisles.value
        val newStore = "A New Store For Testing"

        get<AddLocationUseCase>().invoke(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = newStore,
                pinned = false,
                aisles = emptyList(),
                showDefaultAisle = true
            )
        )

        productViewModel.requestProductAisles()

        val updatedAisles = productViewModel.productAisles.value
        assertEquals(initialAisles.count().inc(), updatedAisles.count())

        assertNotNull(updatedAisles.singleOrNull { it.locationName == newStore })
    }

    @Test
    fun requestProductAisles_NewLocationsUpdated_ExistingEntryUpdated() = runTest {
        val product = productRepository.getAll().first()
        productViewModel.hydrate(product.id, product.inStock)
        val initialAisles = productViewModel.productAisles.value
        val updatedStoreName = "An Updated Store For Testing"

        val initialAisleInfo = initialAisles.first { it.aisleId != 0 }
        get<UpdateLocationUseCase>().invoke(
            get<GetLocationUseCase>().invoke(initialAisleInfo.locationId)!!.copy(
                name = updatedStoreName
            )
        )

        productViewModel.requestProductAisles()

        val updatedAisles = productViewModel.productAisles.value
        assertEquals(initialAisles.count(), updatedAisles.count())

        val updatedAisleInfo = updatedAisles.single { it.locationId == initialAisleInfo.locationId }
        assertEquals(initialAisleInfo.copy(locationName = updatedStoreName), updatedAisleInfo)
    }

    @Test
    fun hydrate_targetAisleIdProvided_productAislesFlowIsUpdated() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }

        productViewModel.hydrate(0, false, aisle.id)

        val productAisles = productViewModel.productAisles.value
        val targetAisleInfo = productAisles.first { it.locationId == aisle.locationId }
        assertEquals(aisle.id, targetAisleInfo.aisleId)
        assertNotEquals(aisle.id, targetAisleInfo.initialAisleId)
    }
}
