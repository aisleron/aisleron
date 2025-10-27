/*
 * Copyright (C) 2025 aisleron.com
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

class UpdateAisleRankUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateAisleRankUseCase: UpdateAisleRankUseCase
    private lateinit var existingAisle: Aisle


    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        val aisleRepository = dm.getRepository<AisleRepository>()
        updateAisleRankUseCase = dm.getUseCase()
        existingAisle = runBlocking { aisleRepository.getAll().first { !it.isDefault } }
    }

    @Test
    fun updateAisleRank_NewRankProvided_AisleRankUpdated() = runTest {
        val updateAisle = existingAisle.copy(rank = 1001)

        updateAisleRankUseCase(updateAisle)

        val updatedAisle: Aisle? = dm.getRepository<AisleRepository>().get(existingAisle.id)
        assertEquals(updateAisle, updatedAisle)
    }

    @Test
    fun updateAisleRank_AisleRankUpdated_OtherAislesMoved() = runTest {
        val updateAisle = existingAisle.copy(rank = existingAisle.rank + 1)
        val maxAisleRankBefore: Int = dm.getRepository<AisleRepository>().getAll()
            .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }

        updateAisleRankUseCase(updateAisle)

        val maxAisleRankAfter: Int = dm.getRepository<AisleRepository>().getAll()
            .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }

        assertEquals(maxAisleRankBefore + 1, maxAisleRankAfter)
    }
}