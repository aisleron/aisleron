package com.aisleron.ui.product

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
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
        productViewModel.saveProduct(updatedProductName, inStock)

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
        productViewModel.saveProduct(newProductName, inStock)
        val newProduct = productRepository.getByName(newProductName)
        val countAfter: Int = productRepository.getAll().count()

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
        val existingProduct: Product = get<ProductRepository>().getAll().first()

        productViewModel.hydrate(existingProduct.id, existingProduct.inStock)
        productViewModel.saveProduct(updatedProductName, inStock)

        Assert.assertEquals(
            ProductViewModel.ProductUiState.Updated(productViewModel),
            productViewModel.productUiState.value
        )
    }

    @Test
    fun testSaveProduct_AisleronErrorOnSave_UiStateIsError() = runTest {
        val existingProduct: Product = get<ProductRepository>().getAll().first()

        productViewModel.hydrate(0, false)
        productViewModel.saveProduct(existingProduct.name, inStock)

        Assert.assertTrue(productViewModel.productUiState.value is ProductViewModel.ProductUiState.Error)
    }

    @Test
    fun testSaveProduct_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on save Product"

        declare<AddProductUseCase> {
            object : AddProductUseCase {
                override suspend fun invoke(product: Product): Int {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val pvm = get<ProductViewModel>()

        pvm.hydrate(0, false)
        pvm.saveProduct("Bogus Product", inStock)

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
        val pvm = ProductViewModel(
            get<AddProductUseCase>(),
            get<UpdateProductUseCase>(),
            get<GetProductUseCase>()
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