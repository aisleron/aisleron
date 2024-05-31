package com.aisleron.ui.product

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class ProductViewModelTest {
    private lateinit var testData: TestDataManager
    private lateinit var productViewModel: ProductViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        testData = TestDataManager()

        val getDefaultAislesUseCase = GetDefaultAislesUseCase(testData.aisleRepository)
        val addAisleProductsUseCase = AddAisleProductsUseCase(testData.aisleProductRepository)
        val isProductNameUniqueUseCase = IsProductNameUniqueUseCase(testData.productRepository)

        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        productViewModel = ProductViewModel(
            AddProductUseCase(
                testData.productRepository,
                getDefaultAislesUseCase,
                addAisleProductsUseCase,
                isProductNameUniqueUseCase
            ),
            UpdateProductUseCase(testData.productRepository, isProductNameUniqueUseCase),
            GetProductUseCase(testData.productRepository),
            testScope
        )
    }

    @Test
    fun testSaveProduct_ProductExists_UpdateProduct() = runTest {
        val updatedProductName = "Updated Product Name"
        val existingProduct: Product = testData.productRepository.getAll().first()
        val countBefore: Int = testData.productRepository.getAll().count()

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.saveProduct(updatedProductName, !existingProduct.inStock)

        val updatedProduct = testData.productRepository.get(existingProduct.id)
        val countAfter: Int = testData.productRepository.getAll().count()

        Assert.assertNotNull(updatedProduct)
        Assert.assertEquals(updatedProductName, updatedProduct?.name)
        Assert.assertEquals(!existingProduct.inStock, updatedProduct?.inStock)
        Assert.assertEquals(countBefore, countAfter)
    }

    @Test
    fun testSaveProduct_ProductDoesNotExists_CreateProduct() = runTest{
        val newProductName = "New Product Name"
        val inStock = false

        productViewModel.hydrate(0, inStock)
        val countBefore: Int = testData.productRepository.getAll().count()
        productViewModel.saveProduct(newProductName, inStock)
        val newProduct = testData.productRepository.getByName(newProductName)
        val countAfter: Int = testData.productRepository.getAll().count()

        Assert.assertNotNull(newProduct)
        Assert.assertEquals(newProductName, newProduct?.name)
        Assert.assertEquals(inStock, newProduct?.inStock)
        Assert.assertEquals(newProductName, productViewModel.productName)
        Assert.assertEquals(inStock, productViewModel.inStock)
        Assert.assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun getProductName() {
    }

    @Test
    fun getInStock() {
    }

    @Test
    fun getProductUiState() {
    }

    @Test
    fun hydrate() {
    }
}