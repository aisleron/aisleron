package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
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
        removeDefaultAisleUseCase = RemoveDefaultAisleUseCase(
            testData.aisleRepository,
            RemoveProductsFromAisleUseCase(testData.aisleProductRepository),
        )

        existingAisle = runBlocking { testData.aisleRepository.getDefaultAisles().first() }
    }

    @Test
    fun removeDefaultAisle_IsDefaultAisle_AisleRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedAisle: Aisle?
        runBlocking {
            countBefore = testData.aisleRepository.getAll().count()
            removeDefaultAisleUseCase(existingAisle)
            removedAisle = testData.aisleRepository.getDefaultAisleFor(existingAisle.id)
            countAfter = testData.aisleRepository.getAll().count()
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
            aisleProductCount =
                testData.aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }

            aisleProductCountBefore = testData.aisleProductRepository.getAll().count()

            removeDefaultAisleUseCase(existingAisle)

            aisleProductCountAfter = testData.aisleProductRepository.getAll().count()

        }
        Assertions.assertEquals(aisleProductCountBefore - aisleProductCount, aisleProductCountAfter)
    }
}