package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.product.ProductRepository
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
            RemoveProductsFromAisleUseCase(testData.getRepository<AisleProductRepository>())
    }

    @Test
    fun removeProductsFromAisle_IsExistingAisle_ProductsRemovedFromAisle() {
        val countBefore: Int
        val countAfter: Int
        val productsCount: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            val aisleId = aisleProductRepository.getAll().first().aisleId
            val aisle = testData.getRepository<AisleRepository>().get(aisleId)!!
            productsCount = aisleProductRepository.getAll().count { it.aisleId == aisleId }
            countBefore = aisleProductRepository.getAll().count()
            removeProductsFromAisleUseCase(aisle)
            countAfter = aisleProductRepository.getAll().count()
        }
        assertEquals(countBefore - productsCount, countAfter)
    }

    @Test
    fun removeProductsFromAisle_AisleProductsRemoved_ProductsNotRemovedFromRepo() {
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            val aisleId = testData.getRepository<AisleProductRepository>().getAll().first().aisleId
            val aisle = testData.getRepository<AisleRepository>().get(aisleId)!!
            countBefore = productRepository.getAll().count()
            removeProductsFromAisleUseCase(aisle)
            countAfter = productRepository.getAll().count()
        }
        assertEquals(countBefore, countAfter)
    }
}