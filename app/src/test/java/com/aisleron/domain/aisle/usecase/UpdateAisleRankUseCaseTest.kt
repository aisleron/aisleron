package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
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
        val aisleRepository = testData.getRepository<AisleRepository>()
        updateAisleRankUseCase = UpdateAisleRankUseCase(aisleRepository)
        existingAisle = runBlocking { aisleRepository.getAll().first { !it.isDefault } }
    }

    @Test
    fun updateAisleRank_NewRankProvided_AisleRankUpdated() {
        val updateAisle = existingAisle.copy(rank = 1001)
        val updatedAisle: Aisle?
        runBlocking {
            updateAisleRankUseCase(updateAisle)
            updatedAisle = testData.getRepository<AisleRepository>().get(existingAisle.id)
        }
        assertNotNull(updatedAisle)
        assertEquals(updateAisle, updatedAisle)
    }

    @Test
    fun updateAisleRank_AisleRankUpdated_OtherAislesMoved() {
        val updateAisle = existingAisle.copy(rank = existingAisle.rank + 1)
        val maxAisleRankBefore: Int
        val maxAisleRankAfter: Int
        runBlocking {
            maxAisleRankBefore = testData.getRepository<AisleRepository>().getAll()
                .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }

            updateAisleRankUseCase(updateAisle)

            maxAisleRankAfter = testData.getRepository<AisleRepository>().getAll()
                .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }
        }
        assertEquals(maxAisleRankBefore + 1, maxAisleRankAfter)
    }
}