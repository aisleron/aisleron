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

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAisleProductsUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var updateAisleProductsUseCase: UpdateAisleProductsUseCase
    private lateinit var existingAisleProduct: AisleProduct
    private lateinit var aisleProductRepository: AisleProductRepository

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        aisleProductRepository = testData.getRepository<AisleProductRepository>()
        updateAisleProductsUseCase = UpdateAisleProductsUseCase(aisleProductRepository)
        existingAisleProduct = runBlocking { aisleProductRepository.getAll().first() }
    }

    @Test
    fun updateProduct_ProductExists_RecordUpdated() {
        val updateAisleProduct = existingAisleProduct.copy(rank = 10)
        val updatedAisleProduct: AisleProduct?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = aisleProductRepository.getAll().count()
            updateAisleProductsUseCase(listOf(updateAisleProduct))
            updatedAisleProduct = aisleProductRepository.get(existingAisleProduct.id)
            countAfter = aisleProductRepository.getAll().count()
        }
        assertNotNull(updatedAisleProduct)
        assertEquals(countBefore, countAfter)
        assertEquals(updateAisleProduct, updatedAisleProduct)
    }

    @Test
    fun updateProduct_ProductDoesNotExist_RecordCreated() {
        val newAisleProduct = existingAisleProduct.copy(
            id = 0,
            rank = 15
        )
        val updatedAisleProduct: AisleProduct?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = aisleProductRepository.getAll().count()
            updateAisleProductsUseCase(listOf(newAisleProduct))
            updatedAisleProduct = aisleProductRepository.getAll().maxBy { it.id }
            countAfter = aisleProductRepository.getAll().count()
        }
        assertNotNull(updatedAisleProduct)
        assertEquals(countBefore + 1, countAfter)
        assertEquals(newAisleProduct.aisleId, updatedAisleProduct?.aisleId)
        assertEquals(newAisleProduct.rank, updatedAisleProduct?.rank)
        assertEquals(newAisleProduct.product, updatedAisleProduct?.product)
    }
}