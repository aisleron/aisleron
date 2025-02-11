package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAisleProductRankUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var updateAisleProductRankUseCase: UpdateAisleProductRankUseCase
    private lateinit var existingAisleProduct: AisleProduct

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val aisleProductRepository = testData.getRepository<AisleProductRepository>()
        updateAisleProductRankUseCase = UpdateAisleProductRankUseCase(aisleProductRepository)
        existingAisleProduct = runBlocking { aisleProductRepository.getAll().first() }
    }

    @Test
    fun updateAisleProductRank_NewRankProvided_AisleProductRankUpdated() {
        val updateAisleProduct = existingAisleProduct.copy()
        updateAisleProduct.rank = 1001
        val updatedAisleProduct: AisleProduct?
        runBlocking {
            updateAisleProductRankUseCase(updateAisleProduct)
            updatedAisleProduct =
                testData.getRepository<AisleProductRepository>().get(existingAisleProduct.id)
        }
        Assertions.assertNotNull(updatedAisleProduct)
        Assertions.assertEquals(updateAisleProduct, updatedAisleProduct)
    }

    @Test
    fun updateAisleProductRank_AisleProductRankUpdated_OtherAisleProductsMoved() {
        val updateAisleProduct = existingAisleProduct.copy(rank = existingAisleProduct.rank + 1)
        val maxAisleProductRankBefore: Int
        val maxAisleProductRankAfter: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            maxAisleProductRankBefore = aisleProductRepository.getAll()
                .filter { it.aisleId == existingAisleProduct.aisleId }.maxOf { it.rank }

            updateAisleProductRankUseCase(updateAisleProduct)

            maxAisleProductRankAfter = aisleProductRepository.getAll()
                .filter { it.aisleId == existingAisleProduct.aisleId }.maxOf { it.rank }
        }
        Assertions.assertEquals(maxAisleProductRankBefore + 1, maxAisleProductRankAfter)
    }
}