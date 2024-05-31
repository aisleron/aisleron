package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
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

        addProductUseCase = AddProductUseCase(
            testData.productRepository,
            GetDefaultAislesUseCase(testData.aisleRepository),
            AddAisleProductsUseCase(testData.aisleProductRepository),
            IsProductNameUniqueUseCase(testData.productRepository)
        )

        existingProduct = runBlocking {
            testData.productRepository.getAll()[1]
        }
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun addProduct_IsDuplicateName_ThrowsException() {
        runBlocking {
            val newProduct = testData.productRepository.getAll()[1].copy(id = 0)
            assertThrows<AisleronException.DuplicateLocationNameException> {
                addProductUseCase(newProduct)
            }
        }
    }

    @Test
    fun addProduct_IsExistingProduct_ProductUpdated() {
        val updateProduct = existingProduct.copy(
            name = existingProduct.name + " Updated",
            inStock = !existingProduct.inStock
        )
        val updatedProduct: Product?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = testData.productRepository.getAll().count()
            val id = addProductUseCase(updateProduct)
            updatedProduct = testData.productRepository.get(id)
            countAfter = testData.productRepository.getAll().count()
        }
        Assertions.assertNotNull(updatedProduct)
        Assertions.assertEquals(countBefore, countAfter)
        Assertions.assertEquals(updateProduct.id, updatedProduct?.id)
        Assertions.assertEquals(updateProduct.name, updatedProduct?.name)
        Assertions.assertEquals(updateProduct.inStock, updatedProduct?.inStock)
    }

    @Test
    fun addProduct_ProductUpdated_DoesNotAddAisleProducts() {
        //TODO("Fix this Test")
        /*val updateProduct = existingProduct.copy(
            name = existingProduct.name + " Updated",
            inStock = !existingProduct.inStock
        )
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        runBlocking {
            aisleProductCountBefore = testData.aisleProductRepository.getAll().count()
            addProductUseCase(updateProduct)
            aisleProductCountAfter = testData.aisleProductRepository.getAll().count()
        }
        Assertions.assertEquals(aisleProductCountBefore, aisleProductCountAfter)*/
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
            countBefore = testData.productRepository.getAll().count()
            val id = addProductUseCase(newProduct)
            insertedProduct = testData.productRepository.get(id)
            countAfter = testData.productRepository.getAll().count()
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
            locationCount = testData.locationRepository.getAll().count()
            aisleProductCountBefore = testData.aisleProductRepository.getAll().count()
            addProductUseCase(newProduct)
            aisleProductCountAfter = testData.aisleProductRepository.getAll().count()
        }
        Assertions.assertEquals(aisleProductCountBefore + locationCount, aisleProductCountAfter)
    }
}