package com.aisleron.ui.product

import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(value = Parameterized::class)
class ProductViewModelTest(private val inStock: Boolean) {
    private lateinit var testData: TestDataManager
    private lateinit var productViewModel: ProductViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        testData = TestDataManager()
        val testUseCases = TestUseCaseProvider(testData)
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        productViewModel = ProductViewModel(
            testUseCases.addProductUseCase,
            testUseCases.updateProductUseCase,
            testUseCases.getProductUseCase,
            testScope
        )
    }

    @Test
    fun testSaveProduct_ProductExists_UpdateProduct() = runTest {
        val updatedProductName = "Updated Product Name"
        val existingProduct: Product = testData.productRepository.getAll().first()
        val countBefore: Int = testData.productRepository.getAll().count()

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.saveProduct(updatedProductName, inStock)

        val updatedProduct = testData.productRepository.get(existingProduct.id)
        val countAfter: Int = testData.productRepository.getAll().count()

        Assert.assertNotNull(updatedProduct)
        Assert.assertEquals(updatedProductName, updatedProduct?.name)
        Assert.assertEquals(inStock, updatedProduct?.inStock)
        Assert.assertEquals(countBefore, countAfter)
    }

    @Test
    fun testSaveProduct_ProductDoesNotExists_CreateProduct() = runTest {
        val newProductName = "New Product Name"

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
    fun testSaveProduct_SaveSuccessful_UiStateIsSuccess() = runTest {
        val updatedProductName = "Updated Product Name"
        val existingProduct: Product = testData.productRepository.getAll().first()

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.saveProduct(updatedProductName, inStock)

        Assert.assertEquals(
            ProductViewModel.ProductUiState.Updated(productViewModel),
            productViewModel.productUiState.value
        )
    }

    @Test
    fun testSaveProduct_AisleronErrorOnSave_UiStateIsError() = runTest {
        val existingProduct: Product = testData.productRepository.getAll().first()

        productViewModel.hydrate(0, false)
        productViewModel.saveProduct(existingProduct.name, inStock)

        Assert.assertTrue(productViewModel.productUiState.value is ProductViewModel.ProductUiState.Error)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSaveProduct_ExceptionRaised_UiStateIsError() = runTest {
        val testUseCases = TestUseCaseProvider(testData)
        val exceptionMessage = "Error on save Product"
        val pvm = ProductViewModel(
            object : AddProductUseCase {
                override suspend fun invoke(product: Product): Int {
                    throw Exception(exceptionMessage)
                }
            },
            testUseCases.updateProductUseCase,
            testUseCases.getProductUseCase,
            TestScope(UnconfinedTestDispatcher())
        )

        pvm.hydrate(0, false)
        pvm.saveProduct("Bogus Product", inStock)

        Assert.assertTrue(pvm.productUiState.value is ProductViewModel.ProductUiState.Error)
        Assert.assertEquals(
            AisleronException.GENERIC_EXCEPTION,
            (pvm.productUiState.value as ProductViewModel.ProductUiState.Error).errorCode
        )
        Assert.assertEquals(
            exceptionMessage,
            (pvm.productUiState.value as ProductViewModel.ProductUiState.Error).errorMessage
        )
    }

    @Test
    fun testGetProductName_ProductExists_ReturnsProductName() = runTest {
        val existingProduct: Product = testData.productRepository.getAll().first()
        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        Assert.assertEquals(existingProduct.name, productViewModel.productName)
    }

    @Test
    fun testGetProductName_ProductDoesNotExists_ReturnsNullProductName() = runTest {
        productViewModel.hydrate(0, false)
        Assert.assertNull(productViewModel.productName)
    }

    @Test
    fun testHydrate_ProductDoesNotExists_UiStateIsUpdated() = runTest {
        productViewModel.hydrate(1, inStock)
        Assert.assertTrue(productViewModel.productUiState.value is ProductViewModel.ProductUiState.Updated)
        Assert.assertEquals(
            productViewModel,
            (productViewModel.productUiState.value as ProductViewModel.ProductUiState.Updated).product
        )
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ProductViewModelReturned() {
        val testUseCases = TestUseCaseProvider(testData)
        val pvm = ProductViewModel(
            testUseCases.addProductUseCase,
            testUseCases.updateProductUseCase,
            testUseCases.getProductUseCase
        )

        Assert.assertNotNull(pvm)
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