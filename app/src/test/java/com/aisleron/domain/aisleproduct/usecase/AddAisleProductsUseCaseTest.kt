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
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddAisleProductsUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var addAisleProductsUseCase: AddAisleProductsUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        addAisleProductsUseCase = dm.getUseCase()
    }

    @Test
    fun addAisleProduct_IsExistingAisleProduct_AisleProductUpdated() = runTest {
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val existingAisleProduct = aisleProductRepository.getAll().first()
        val updateAisleProduct = existingAisleProduct.copy(
            rank = existingAisleProduct.rank + 10
        )

        val countBefore: Int = aisleProductRepository.getAll().count()
        val id = addAisleProductsUseCase(listOf(updateAisleProduct)).first()

        val updatedAisleProduct = aisleProductRepository.get(id)

        Assertions.assertNotNull(updatedAisleProduct)
        Assertions.assertEquals(updateAisleProduct.id, updatedAisleProduct?.id)
        Assertions.assertEquals(updateAisleProduct.aisleId, updatedAisleProduct?.aisleId)
        Assertions.assertEquals(updateAisleProduct.rank, updatedAisleProduct?.rank)
        Assertions.assertEquals(updateAisleProduct.product, updatedAisleProduct?.product)

        val countAfter: Int = aisleProductRepository.getAll().count()
        Assertions.assertEquals(countBefore, countAfter)
    }

    private fun getNewAisleProduct(): AisleProduct {
        val newAisle = Aisle(
            name = "AisleProductTest Aisle",
            products = emptyList(),
            locationId = runBlocking {
                dm.getRepository<LocationRepository>().getAll().first().id
            },
            rank = 1,
            isDefault = false,
            id = 0,
            expanded = true
        )

        return AisleProduct(
            id = 0,
            aisleId = runBlocking { dm.getRepository<AisleRepository>().add(newAisle) },
            rank = 1,
            product = runBlocking { dm.getRepository<ProductRepository>().getAll().first() }
        )
    }

    @Test
    fun addAisleProduct_IsNewAisleProduct_AisleProductCreated() = runTest {
        val newAisleProduct = getNewAisleProduct()
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val countBefore = aisleProductRepository.getAll().count()

        val id = addAisleProductsUseCase(listOf(newAisleProduct)).first()

        val insertedAisleProduct = aisleProductRepository.get(id)
        Assertions.assertEquals(newAisleProduct.product, insertedAisleProduct?.product)
        Assertions.assertEquals(newAisleProduct.aisleId, insertedAisleProduct?.aisleId)
        Assertions.assertEquals(newAisleProduct.rank, insertedAisleProduct?.rank)

        val countAfter = aisleProductRepository.getAll().count()
        Assertions.assertEquals(countBefore + 1, countAfter)
    }
}