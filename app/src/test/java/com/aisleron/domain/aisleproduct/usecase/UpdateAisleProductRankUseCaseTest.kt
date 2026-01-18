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
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAisleProductRankUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateAisleProductRankUseCase: UpdateAisleProductRankUseCase
    private lateinit var existingAisleProduct: AisleProduct

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        updateAisleProductRankUseCase = dm.getUseCase()
        existingAisleProduct = runBlocking { aisleProductRepository.getAll().first() }
    }

    @Test
    fun updateAisleProductRank_NewRankProvided_AisleProductRankUpdated() = runTest {
        val updateAisleProduct = existingAisleProduct.copy(rank = 1001)

        updateAisleProductRankUseCase(updateAisleProduct)

        val updatedAisleProduct =
            dm.getRepository<AisleProductRepository>().get(existingAisleProduct.id)

        Assertions.assertEquals(updateAisleProduct, updatedAisleProduct)
    }

    @Test
    fun updateAisleProductRank_AisleProductRankUpdated_OtherAisleProductsMoved() = runTest {
        val updateAisleProduct = existingAisleProduct.copy(rank = existingAisleProduct.rank + 1)
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val maxAisleProductRankBefore = aisleProductRepository.getAll()
            .filter { it.aisleId == existingAisleProduct.aisleId }.maxOf { it.rank }

        updateAisleProductRankUseCase(updateAisleProduct)

        val maxAisleProductRankAfter = aisleProductRepository.getAll()
            .filter { it.aisleId == existingAisleProduct.aisleId }.maxOf { it.rank }

        Assertions.assertEquals(maxAisleProductRankBefore + 1, maxAisleProductRankAfter)
    }
}