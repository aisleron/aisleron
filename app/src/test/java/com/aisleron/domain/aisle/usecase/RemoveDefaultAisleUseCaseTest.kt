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