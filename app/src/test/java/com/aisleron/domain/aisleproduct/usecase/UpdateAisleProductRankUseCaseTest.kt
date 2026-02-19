/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class UpdateAisleProductRankUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateAisleProductRankUseCase: UpdateAisleProductRankUseCase
    private lateinit var existingAisleProduct: AisleProduct
    private lateinit var aisleProductRepository: AisleProductRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        aisleProductRepository = dm.getRepository<AisleProductRepository>()
        updateAisleProductRankUseCase = dm.getUseCase()
        existingAisleProduct = runBlocking { aisleProductRepository.getAll().first() }
    }

    @Test
    fun updateAisleProductRank_NewRankProvided_AisleProductRankUpdated() = runTest {
        val newRank = 1001
        val newAisle = existingAisleProduct.aisleId

        updateAisleProductRankUseCase(existingAisleProduct.id, newRank, newAisle)

        val updatedAisleProduct = aisleProductRepository.get(existingAisleProduct.id)
        assertEquals(existingAisleProduct.copy(rank = newRank), updatedAisleProduct)
    }

    @Test
    fun updateAisleProductRank_AisleProductRankUpdated_OtherAisleProductsMoved() = runTest {
        val newRank = existingAisleProduct.rank + 1
        val newAisle = existingAisleProduct.aisleId
        val maxAisleProductRankBefore = aisleProductRepository.getAll()
            .filter { it.aisleId == existingAisleProduct.aisleId }.maxOf { it.rank }

        updateAisleProductRankUseCase(existingAisleProduct.id, newRank, newAisle)

        val maxAisleProductRankAfter = aisleProductRepository.getAll()
            .filter { it.aisleId == existingAisleProduct.aisleId }.maxOf { it.rank }

        assertEquals(maxAisleProductRankBefore + 1, maxAisleProductRankAfter)
    }

    @Test
    fun updateAisleProductRank_InvalidIdProvided_NoAislesUpdated() = runTest {
        val newRank = 1001
        val newAisle = existingAisleProduct.aisleId

        updateAisleProductRankUseCase(-1, newRank, newAisle)

        val updatedAisleProduct = aisleProductRepository.getAll().firstOrNull { it.rank == newRank }
        assertNull(updatedAisleProduct)
    }


    @Test
    fun updateAisleProductRank_NewAisleProvided_AisleProductAisleUpdated() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val locationId = aisleRepository.get(existingAisleProduct.aisleId)!!.locationId
        val newAisle = aisleRepository.getAll()
            .first { it.locationId == locationId && it.id != existingAisleProduct.aisleId }.id

        updateAisleProductRankUseCase(
            existingAisleProduct.id,
            existingAisleProduct.rank,
            newAisle
        )

        val updatedAisleProduct = aisleProductRepository.get(existingAisleProduct.id)
        assertEquals(existingAisleProduct.copy(aisleId = newAisle), updatedAisleProduct)
    }

    /**
     *
     * Test invalid aisle
     */
}