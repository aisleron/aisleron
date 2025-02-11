package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddProductUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var addProductUseCase: AddProductUseCase
    private lateinit var existingProduct: Product

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val productRepository = testData.getRepository<ProductRepository>()

        addProductUseCase = AddProductUseCaseImpl(
            productRepository,
            GetDefaultAislesUseCase(testData.getRepository<AisleRepository>()),
            AddAisleProductsUseCase(testData.getRepository<AisleProductRepository>()),
            IsProductNameUniqueUseCase(testData.getRepository<ProductRepository>())
        )

        existingProduct = runBlocking {
            productRepository.getAll()[1]
        }
    }

    @Test
    fun addProduct_IsDuplicateName_ThrowsException() {
        runBlocking {
            val newProduct = testData.getRepository<ProductRepository>().getAll()[1].copy(id = 0)
            assertThrows<AisleronException.DuplicateProductNameException> {
                addProductUseCase(newProduct)
            }
        }
    }

    @Test
    fun addProduct_IsExistingProduct_ThrowsException() {
        val updateProduct = existingProduct.copy(
            name = existingProduct.name + " Updated",
            inStock = !existingProduct.inStock
        )

        runBlocking {
            assertThrows<AisleronException.DuplicateProductException> {
                addProductUseCase(updateProduct)
            }
        }
    }

    private fun getNewProduct(): Product {
        return Product(
            id = 0,
            name = "New Product 1",
            inStock = false
        )
    }

    @Test
    fun addProduct_IsNewProduct_ProductCreated() {
        val newProduct = getNewProduct()
        val countBefore: Int
        val countAfter: Int
        val insertedProduct: Product?
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            countBefore = productRepository.getAll().count()
            val id = addProductUseCase(newProduct)
            insertedProduct = productRepository.get(id)
            countAfter = productRepository.getAll().count()
        }
        Assertions.assertNotNull(insertedProduct)
        Assertions.assertEquals(countBefore + 1, countAfter)
        Assertions.assertEquals(newProduct.name, insertedProduct?.name)
        Assertions.assertEquals(newProduct.inStock, insertedProduct?.inStock)
    }

    @Test
    fun addProduct_ProductInserted_AddsAisleProducts() {
        val newProduct = getNewProduct()
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val locationCount: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            locationCount = testData.getRepository<LocationRepository>().getAll().count()
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            addProductUseCase(newProduct)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
        }
        Assertions.assertEquals(aisleProductCountBefore + locationCount, aisleProductCountAfter)
    }
}