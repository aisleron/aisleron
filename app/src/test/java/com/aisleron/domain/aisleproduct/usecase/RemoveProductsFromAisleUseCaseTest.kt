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
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveProductsFromAisleUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var removeProductsFromAisleUseCase: RemoveProductsFromAisleUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        removeProductsFromAisleUseCase =
            RemoveProductsFromAisleUseCase(testData.getRepository<AisleProductRepository>())
    }

    @Test
    fun removeProductsFromAisle_IsExistingAisle_ProductsRemovedFromAisle() {
        val countBefore: Int
        val countAfter: Int
        val productsCount: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            val aisleId = aisleProductRepository.getAll().first().aisleId
            val aisle = testData.getRepository<AisleRepository>().get(aisleId)!!
            productsCount = aisleProductRepository.getAll().count { it.aisleId == aisleId }
            countBefore = aisleProductRepository.getAll().count()
            removeProductsFromAisleUseCase(aisle)
            countAfter = aisleProductRepository.getAll().count()
        }
        assertEquals(countBefore - productsCount, countAfter)
    }

    @Test
    fun removeProductsFromAisle_AisleProductsRemoved_ProductsNotRemovedFromRepo() {
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            val aisleId = testData.getRepository<AisleProductRepository>().getAll().first().aisleId
            val aisle = testData.getRepository<AisleRepository>().get(aisleId)!!
            countBefore = productRepository.getAll().count()
            removeProductsFromAisleUseCase(aisle)
            countAfter = productRepository.getAll().count()
        }
        assertEquals(countBefore, countAfter)
    }
}