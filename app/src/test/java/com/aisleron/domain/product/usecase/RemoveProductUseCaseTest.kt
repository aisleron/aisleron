package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
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
        val productRepository = testData.getRepository<ProductRepository>()

        removeProductUseCase = RemoveProductUseCase(productRepository)

        existingProduct = runBlocking { productRepository.get(1)!! }
    }

    @Test
    fun removeProduct_IsExistingProduct_ProductRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedProduct: Product?
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            countBefore = productRepository.getAll().count()
            removeProductUseCase(existingProduct.id)
            removedProduct = productRepository.get(existingProduct.id)
            countAfter = productRepository.getAll().count()
        }
        assertNull(removedProduct)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeProduct_IsNonExistentProduct_NoProductsRemoved() {
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            countBefore = productRepository.getAll().count()
            removeProductUseCase(productRepository.getAll().maxOf { it.id } + 1000)
            countAfter = productRepository.getAll().count()
        }
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun removeProduct_ProductRemoved_AisleProductsRemoved() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCountProduct: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            val aisleProductList = aisleProductRepository.getAll()
                .filter { it.product.id == existingProduct.id }
            aisleProductCountProduct = aisleProductList.count()
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            removeProductUseCase(existingProduct.id)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
        }
        assertEquals(
            aisleProductCountBefore - aisleProductCountProduct,
            aisleProductCountAfter
        )
    }
}