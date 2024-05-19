package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.data.TestDataManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveProductsFromAisleUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var removeProductsFromAisleUseCase: RemoveProductsFromAisleUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        removeProductsFromAisleUseCase =
            RemoveProductsFromAisleUseCase(testData.aisleProductRepository)
    }

    @Test
    fun removeProductsFromAisle_IsExistingAisle_ProductsRemovedFromAisle() {
        val countBefore: Int
        val countAfter: Int
        val productsCount: Int
        runBlocking {
            val aisleId = testData.aisleProductRepository.getAll().first().aisleId
            val aisle = testData.aisleRepository.get(aisleId)!!
            productsCount = testData.aisleProductRepository.getAll().count { it.aisleId == aisleId }
            countBefore = testData.aisleProductRepository.getAll().count()
            removeProductsFromAisleUseCase(aisle)
            countAfter = testData.aisleProductRepository.getAll().count()
        }
        assertEquals(countBefore - productsCount, countAfter)
    }

    @Test
    fun removeProductsFromAisle_AisleProductsRemoved_ProductsNotRemovedFromRepo() {
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val aisleId = testData.aisleProductRepository.getAll().first().aisleId
            val aisle = testData.aisleRepository.get(aisleId)!!
            countBefore = testData.productRepository.getAll().count()
            removeProductsFromAisleUseCase(aisle)
            countAfter = testData.productRepository.getAll().count()
        }
        assertEquals(countBefore, countAfter)
    }
}