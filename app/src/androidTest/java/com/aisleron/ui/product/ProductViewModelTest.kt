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
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare

@RunWith(value = Parameterized::class)
class ProductViewModelTest(private val inStock: Boolean) : KoinTest {
    private lateinit var productViewModel: ProductViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        productViewModel = get<ProductViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun testSaveProduct_ProductExists_UpdateProduct() = runTest {
        val updatedProductName = "Updated Product Name"
        val productRepository = get<ProductRepository>()
        val existingProduct: Product = productRepository.getAll().first()
        val countBefore: Int = productRepository.getAll().count()

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.updateProductName(updatedProductName)
        productViewModel.updateInStock(inStock)

        productViewModel.saveProduct()

        val updatedProduct = productRepository.get(existingProduct.id)
        val countAfter: Int = productRepository.getAll().count()

        Assert.assertNotNull(updatedProduct)
        Assert.assertEquals(updatedProductName, updatedProduct?.name)
        Assert.assertEquals(inStock, updatedProduct?.inStock)
        Assert.assertEquals(countBefore, countAfter)
    }

    @Test
    fun testSaveProduct_ProductDoesNotExists_CreateProduct() = runTest {
        val newProductName = "New Product Name"
        val productRepository = get<ProductRepository>()

        productViewModel.hydrate(0, inStock)
        val countBefore: Int = productRepository.getAll().count()

        productViewModel.updateProductName(newProductName)
        productViewModel.updateInStock(inStock)

        productViewModel.saveProduct()
        val newProduct = productRepository.getByName(newProductName)
        val countAfter: Int = productRepository.getAll().count()

        Assert.assertNotNull(newProduct)
        Assert.assertEquals(newProductName, newProduct?.name)
        Assert.assertEquals(inStock, newProduct?.inStock)
        Assert.assertEquals(newProductName, productViewModel.uiData.value.productName)
        Assert.assertEquals(inStock, productViewModel.uiData.value.inStock)
        Assert.assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun testSaveProduct_SaveSuccessful_UiStateIsSuccess() = runTest {
        val updatedProductName = "Updated Product Name"
        val existingProduct: Product = get<ProductRepository>().getAll().first()

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.updateProductName(updatedProductName)
        productViewModel.updateInStock(inStock)
        productViewModel.saveProduct()

        Assert.assertEquals(
            ProductViewModel.ProductUiState.Success, productViewModel.productUiState.value
        )
    }

    @Test
    fun testSaveProduct_ProductNameIsBlank_NoAction() = runTest {
        val updatedProductName = ""

        productViewModel.hydrate(0, inStock)
        productViewModel.updateProductName(updatedProductName)
        productViewModel.saveProduct()

        Assert.assertEquals(
            ProductViewModel.ProductUiState.Empty, productViewModel.productUiState.value
        )
    }

    @Test
    fun testSaveProduct_AisleronErrorOnSave_UiStateIsError() = runTest {
        val existingProduct: Product = get<ProductRepository>().getAll().first()

        productViewModel.hydrate(0, false)
        productViewModel.updateProductName(existingProduct.name)
        productViewModel.updateInStock(inStock)
        productViewModel.saveProduct()

        Assert.assertTrue(productViewModel.productUiState.value is ProductViewModel.ProductUiState.Error)
    }

    @Test
    fun testSaveProduct_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on save Product"

        declare<AddProductUseCase> {
            object : AddProductUseCase {
                override suspend fun invoke(product: Product, targetAisle: Aisle?): Int {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val pvm = get<ProductViewModel>()

        pvm.hydrate(0, false)
        pvm.updateProductName("Bogus Product")
        pvm.updateInStock(inStock)
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
        val existingProduct: Product = get<ProductRepository>().getAll().first()
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
        productViewModel.hydrate(1, inStock)
        Assert.assertTrue(productViewModel.productUiState.value is ProductViewModel.ProductUiState.Empty)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ProductViewModelReturned() {
        val pvm = ProductViewModel(
            get<AddProductUseCase>(),
            get<UpdateProductUseCase>(),
            get<GetProductUseCase>(),
            get<GetAisleUseCase>()
        )

        Assert.assertNotNull(pvm)
    }

    @Test
    fun testSaveProduct_AisleProvided_ProductAddedToAisle() = runTest {
        val newProductName = "New Product Name"
        val productRepository = get<ProductRepository>()
        val aisleProductRepository = get<AisleProductRepository>()
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }

        productViewModel.hydrate(0, inStock, aisle.locationId, aisle.id)
        val countBefore = aisleProductRepository.getAll().count { it.aisleId == aisle.id }
        productViewModel.updateProductName(newProductName)
        productViewModel.updateInStock(inStock)
        productViewModel.saveProduct()
        val newProduct = productRepository.getByName(newProductName)
        val countAfter = aisleProductRepository.getAll().count { it.aisleId == aisle.id }

        Assert.assertNotNull(newProduct)
        Assert.assertEquals(newProductName, newProduct?.name)
        Assert.assertEquals(inStock, newProduct?.inStock)
        Assert.assertEquals(newProductName, productViewModel.uiData.value.productName)
        Assert.assertEquals(inStock, productViewModel.uiData.value.inStock)
        Assert.assertEquals(countBefore + 1, countAfter)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(true),
                arrayOf(false)
            )
        }
    }
}