package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.product.Product
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateProductStatusUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var updateProductStatusUseCase: UpdateProductStatusUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        updateProductStatusUseCase = UpdateProductStatusUseCase(
            GetProductUseCase(testData.productRepository),
            UpdateProductUseCase(
                testData.productRepository,
                IsProductNameUniqueUseCase(testData.productRepository)
            )
        )
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun updateProductStatus_ProductExists_StatusUpdated() {
        val existingProduct: Product
        val updatedProduct = runBlocking {
            existingProduct = testData.productRepository.getAll().first()
            updateProductStatusUseCase(existingProduct.id, !existingProduct.inStock)
        }

        assertNotNull(updatedProduct)
        assertEquals(existingProduct.id, updatedProduct?.id)
        assertEquals(existingProduct.name, updatedProduct?.name)
        assertEquals(!existingProduct.inStock, updatedProduct?.inStock)
    }

    @Test
    fun updateProductStatus_ProductDoesNotExist_ReturnNull () {
        val updatedProduct = runBlocking {
            updateProductStatusUseCase(1001, true)
        }

        assertNull(updatedProduct)
    }
}