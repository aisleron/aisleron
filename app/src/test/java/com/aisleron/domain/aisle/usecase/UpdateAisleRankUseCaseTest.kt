package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAisleRankUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var updateAisleRankUseCase: UpdateAisleRankUseCase
    private lateinit var existingAisle: Aisle


    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        updateAisleRankUseCase = UpdateAisleRankUseCase(testData.aisleRepository)
        existingAisle = runBlocking { testData.aisleRepository.getAll().first{ !it.isDefault } }
    }

    @Test
    fun updateAisleRank_NewRankProvided_AisleUpdated() {
        val updateAisle = existingAisle.copy(rank = 1001)
        val updatedAisle: Aisle?
        runBlocking {
            updateAisleRankUseCase(updateAisle)
            updatedAisle = testData.aisleRepository.get(existingAisle.id)
        }
        assertNotNull(updatedAisle)
        assertEquals(updateAisle, updatedAisle)
    }

    @Test
    fun updateAisleRank_AisleRankChanged_OtherAislesMoved() {
        val updateAisle = existingAisle.copy(rank = existingAisle.rank + 1)
        val maxAisleRankBefore: Int
        val maxAisleRankAfter: Int
        runBlocking {
            maxAisleRankBefore = testData.aisleRepository.getAll()
                .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }

            updateAisleRankUseCase(updateAisle)

            maxAisleRankAfter = testData.aisleRepository.getAll()
                .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }
        }
        assertEquals(maxAisleRankBefore + 1, maxAisleRankAfter)
    }
}