package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.product.Product
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsProductNameUniqueUseCaseTest {

    private lateinit var isProductNameUniqueUseCase: IsProductNameUniqueUseCase

    @BeforeEach
    fun setUp() {
        isProductNameUniqueUseCase = IsProductNameUniqueUseCase(testData.productRepository)
    }

    @AfterEach
    fun tearDown() {

    }

    @Test
    fun isNameUnique_NoMatchingNameExists_ReturnTrue() {
        val newProduct = existingProduct.copy(id = 0, name = "Product Unique Name")
        val result = runBlocking {
            isProductNameUniqueUseCase(newProduct)
        }
        Assertions.assertNotEquals(existingProduct.name, newProduct.name)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_LocationIdsMatch_ReturnTrue() {
        val newProduct = existingProduct.copy(inStock = true)
        val result = runBlocking {
            isProductNameUniqueUseCase(newProduct)
        }
        Assertions.assertEquals(existingProduct.id, newProduct.id)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_NamesMatchIdsDiffer_ReturnFalse() {
        val newProduct = existingProduct.copy(id = 0)
        val result = runBlocking {
            isProductNameUniqueUseCase(newProduct)
        }
        Assertions.assertEquals(existingProduct.name, newProduct.name)
        Assertions.assertNotEquals(existingProduct.id, newProduct.id)
        Assertions.assertFalse(result)
    }

    companion object {

        private lateinit var testData: TestDataManager
        private lateinit var existingProduct: Product

        @JvmStatic
        @BeforeAll
        fun beforeSpec() {
            testData = TestDataManager()

            existingProduct = runBlocking {
                testData.productRepository.get(1)!!
            }
        }
    }
}