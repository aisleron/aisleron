package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveDefaultAisleUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var removeDefaultAisleUseCase: RemoveDefaultAisleUseCase
    private lateinit var existingAisle: Aisle

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val aisleRepository = testData.getRepository<AisleRepository>()
        removeDefaultAisleUseCase = RemoveDefaultAisleUseCase(
            aisleRepository,
            RemoveProductsFromAisleUseCase(testData.getRepository<AisleProductRepository>()),
        )

        existingAisle = runBlocking { aisleRepository.getDefaultAisles().first() }
    }

    @Test
    fun removeDefaultAisle_IsDefaultAisle_AisleRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedAisle: Aisle?
        val aisleRepository = testData.getRepository<AisleRepository>()
        runBlocking {
            countBefore = aisleRepository.getAll().count()
            removeDefaultAisleUseCase(existingAisle)
            removedAisle = aisleRepository.getDefaultAisleFor(existingAisle.id)
            countAfter = aisleRepository.getAll().count()
        }
        Assertions.assertNull(removedAisle)
        Assertions.assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeDefaultAisle_AisleRemoved_RemoveAisleProducts() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCount: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            aisleProductCount =
                aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }

            aisleProductCountBefore = aisleProductRepository.getAll().count()

            removeDefaultAisleUseCase(existingAisle)

            aisleProductCountAfter = aisleProductRepository.getAll().count()

        }
        Assertions.assertEquals(aisleProductCountBefore - aisleProductCount, aisleProductCountAfter)
    }
}