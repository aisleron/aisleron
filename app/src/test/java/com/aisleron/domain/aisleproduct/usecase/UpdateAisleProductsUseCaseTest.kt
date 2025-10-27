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

package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAisleProductsUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateAisleProductsUseCase: UpdateAisleProductsUseCase
    private lateinit var existingAisleProduct: AisleProduct
    private lateinit var aisleProductRepository: AisleProductRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        aisleProductRepository = dm.getRepository<AisleProductRepository>()
        updateAisleProductsUseCase = dm.getUseCase()
        existingAisleProduct = runBlocking { aisleProductRepository.getAll().first() }
    }

    @Test
    fun updateProduct_ProductExists_RecordUpdated() = runTest {
        val updateAisleProduct = existingAisleProduct.copy(rank = 10)
        val countBefore: Int = aisleProductRepository.getAll().count()

        updateAisleProductsUseCase(listOf(updateAisleProduct))

        val updatedAisleProduct: AisleProduct? = aisleProductRepository.get(existingAisleProduct.id)
        assertEquals(updateAisleProduct, updatedAisleProduct)

        val countAfter: Int = aisleProductRepository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun updateProduct_ProductDoesNotExist_RecordCreated() = runTest {
        val newAisleProduct = existingAisleProduct.copy(id = 0, rank = 15)
        val countBefore: Int = aisleProductRepository.getAll().count()

        updateAisleProductsUseCase(listOf(newAisleProduct))

        val updatedAisleProduct = aisleProductRepository.getAll().maxBy { it.id }
        assertEquals(newAisleProduct.rank, updatedAisleProduct.rank)
        assertEquals(newAisleProduct.product, updatedAisleProduct.product)
        assertEquals(newAisleProduct.aisleId, updatedAisleProduct.aisleId)

        val countAfter: Int = aisleProductRepository.getAll().count()
        assertEquals(countBefore + 1, countAfter)
    }
}