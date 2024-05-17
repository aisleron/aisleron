package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.product.Product
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveProductUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var removeProductUseCase: RemoveProductUseCase
    private lateinit var existingProduct: Product

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()

        removeProductUseCase = RemoveProductUseCase(testData.productRepository)

        existingProduct = runBlocking { testData.productRepository.get(1)!! }
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun removeProduct_IsExistingProduct_ProductRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedProduct: Product?
        runBlocking {
            countBefore = testData.productRepository.getAll().count()
            removeProductUseCase(existingProduct.id)
            removedProduct = testData.productRepository.get(existingProduct.id)
            countAfter = testData.productRepository.getAll().count()
        }
        assertNull(removedProduct)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeProduct_ProductRemoved_AisleProductsRemoved() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCountProduct: Int
        runBlocking {
            val aisleProductList = testData.aisleProductRepository.getAll()
                .filter { it.product.id == existingProduct.id }
            aisleProductCountProduct = aisleProductList.count()
            aisleProductCountBefore = testData.aisleProductRepository.getAll().count()
            removeProductUseCase(existingProduct.id)
            aisleProductCountAfter = testData.aisleProductRepository.getAll().count()
        }
        assertEquals(
            aisleProductCountBefore - aisleProductCountProduct,
            aisleProductCountAfter
        )
    }
}