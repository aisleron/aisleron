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
import com.aisleron.domain.aisleproduct.AisleProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveDefaultAisleUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var removeDefaultAisleUseCase: RemoveDefaultAisleUseCase
    private lateinit var existingAisle: Aisle

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        val aisleRepository = dm.getRepository<AisleRepository>()
        removeDefaultAisleUseCase = dm.getUseCase()
        existingAisle = runBlocking { aisleRepository.getDefaultAisles().first() }
    }

    @Test
    fun removeDefaultAisle_IsDefaultAisle_AisleRemoved() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val countBefore = aisleRepository.getAll().count()

        removeDefaultAisleUseCase(existingAisle)

        val removedAisle = aisleRepository.getDefaultAisleFor(existingAisle.id)
        Assertions.assertNull(removedAisle)

        val countAfter = aisleRepository.getAll().count()
        Assertions.assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeDefaultAisle_AisleRemoved_RemoveAisleProducts() = runTest {
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val aisleProductCount =
            aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }

        val aisleProductCountBefore = aisleProductRepository.getAll().count()

        removeDefaultAisleUseCase(existingAisle)

        val aisleProductCountAfter = aisleProductRepository.getAll().count()
        Assertions.assertEquals(aisleProductCountBefore - aisleProductCount, aisleProductCountAfter)
    }
}