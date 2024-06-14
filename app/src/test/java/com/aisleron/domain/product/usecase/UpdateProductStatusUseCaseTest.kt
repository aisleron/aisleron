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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class UpdateProductStatusUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var updateProductStatusUseCase: UpdateProductStatusUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        updateProductStatusUseCase = UpdateProductStatusUseCaseImpl(
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

    @ParameterizedTest(name = "Test when inStock Status is {0}")
    @MethodSource("inStockArguments")
    fun updateProductStatus_ProductExists_StatusUpdated(inStock: Boolean) {
        val existingProduct: Product
        val updatedProduct = runBlocking {
            existingProduct = testData.productRepository.getAll().first()
            updateProductStatusUseCase(existingProduct.id, inStock)
        }

        assertNotNull(updatedProduct)
        assertEquals(existingProduct.id, updatedProduct?.id)
        assertEquals(existingProduct.name, updatedProduct?.name)
        assertEquals(inStock, updatedProduct?.inStock)
    }

    @Test
    fun updateProductStatus_ProductDoesNotExist_ReturnNull() {
        val updatedProduct = runBlocking {
            updateProductStatusUseCase(1001, true)
        }

        assertNull(updatedProduct)
    }

    private companion object {
        @JvmStatic
        fun inStockArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}