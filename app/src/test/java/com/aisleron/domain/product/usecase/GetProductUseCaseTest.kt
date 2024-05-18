package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetProductUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getProductUseCase: GetProductUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()

        getProductUseCase = GetProductUseCase(
            testData.productRepository
        )
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getProduct_NonExistentId_ReturnNull() {
        Assertions.assertNull(runBlocking { getProductUseCase(2001) })
    }

    @Test
    fun getProduct_ExistingId_ReturnProduct() {
        val product = runBlocking { getProductUseCase(1) }
        Assertions.assertNotNull(product)
        Assertions.assertEquals(1, product!!.id)
    }
}