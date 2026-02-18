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

package com.aisleron.domain.aisle.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class UpdateAisleRankUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateAisleRankUseCase: UpdateAisleRankUseCase
    private lateinit var existingAisle: Aisle
    private lateinit var aisleRepository: AisleRepository


    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        updateAisleRankUseCase = dm.getUseCase()
        aisleRepository = dm.getRepository()
        existingAisle = runBlocking { aisleRepository.getAll().first { !it.isDefault } }
    }

    @Test
    fun updateAisleRank_NewRankProvided_AisleRankUpdated() = runTest {
        val newRank = 1001

        updateAisleRankUseCase(existingAisle.id, 1001)

        val updatedAisle: Aisle? = aisleRepository.get(existingAisle.id)
        assertEquals(existingAisle.copy(rank = newRank), updatedAisle)
    }

    @Test
    fun updateAisleRank_AisleRankUpdated_OtherAislesMoved() = runTest {
        val newRank = existingAisle.rank + 1
        val maxAisleRankBefore: Int = aisleRepository.getAll()
            .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }

        updateAisleRankUseCase(existingAisle.id, newRank)

        val maxAisleRankAfter: Int = aisleRepository.getAll()
            .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }

        assertEquals(maxAisleRankBefore + 1, maxAisleRankAfter)
    }

    @Test
    fun updateAisleRank_InvalidIdProvided_NoAislesUpdated() = runTest {
        val newRank = 1001

        updateAisleRankUseCase(-1, 1001)

        val updatedAisle = aisleRepository.getAll().firstOrNull { it.rank == newRank }
        assertNull(updatedAisle)
    }
}