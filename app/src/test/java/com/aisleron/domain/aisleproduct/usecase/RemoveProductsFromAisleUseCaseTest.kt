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
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveProductsFromAisleUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var removeProductsFromAisleUseCase: RemoveProductsFromAisleUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        removeProductsFromAisleUseCase = dm.getUseCase()
    }

    @Test
    fun removeProductsFromAisle_IsExistingAisle_ProductsRemovedFromAisle() = runTest {
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val aisleId = aisleProductRepository.getAll().first().aisleId
        val aisle = dm.getRepository<AisleRepository>().get(aisleId)!!
        val productsCount = aisleProductRepository.getAll().count { it.aisleId == aisleId }
        val countBefore = aisleProductRepository.getAll().count()

        removeProductsFromAisleUseCase(aisle)

        val countAfter = aisleProductRepository.getAll().count()
        assertEquals(countBefore - productsCount, countAfter)
    }

    @Test
    fun removeProductsFromAisle_AisleProductsRemoved_ProductsNotRemovedFromRepo() = runTest {
        val productRepository = dm.getRepository<ProductRepository>()
        val aisleId = dm.getRepository<AisleProductRepository>().getAll().first().aisleId
        val aisle = dm.getRepository<AisleRepository>().get(aisleId)!!
        val countBefore = productRepository.getAll().count()

        removeProductsFromAisleUseCase(aisle)

        val countAfter = productRepository.getAll().count()
        assertEquals(countBefore, countAfter)
    }
}