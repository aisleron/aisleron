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
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChangeProductAisleUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var changeProductAisleUseCase: ChangeProductAisleUseCase
    private lateinit var aisleProductRepository: AisleProductRepository
    private lateinit var aisleRepository: AisleRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var product: Product
    private lateinit var aisleProduct: AisleProduct

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        changeProductAisleUseCase = dm.getUseCase()
        aisleProductRepository = dm.getRepository()
        aisleRepository = dm.getRepository()
        productRepository = dm.getRepository()
        runBlocking {
            product = productRepository.getAll().first()
            aisleProduct = aisleProductRepository.getProductAisles(product.id).first()
        }
    }

    @Test
    fun changeProductAisle_sameAisle_doesNothing() = runTest {
        val originalAisleProduct = aisleProductRepository.get(aisleProduct.id)

        changeProductAisleUseCase(product.id, aisleProduct.aisleId, aisleProduct.aisleId)

        val updatedAisleProduct = aisleProductRepository.get(aisleProduct.id)
        assertEquals(originalAisleProduct, updatedAisleProduct)
    }

    @Test
    fun changeProductAisle_differentLocations_throwsException() = runTest {
        val currentAisle = aisleRepository.get(aisleProduct.aisleId)!!
        val anotherLocationAisle =
            aisleRepository.getAll().first { it.locationId != currentAisle.locationId }

        assertThrows<AisleronException.AisleMoveException> {
            changeProductAisleUseCase(product.id, currentAisle.id, anotherLocationAisle.id)
        }
    }

    @Test
    fun changeProductAisle_productNotInAisle_doesNothing() = runTest {
        val anotherProduct = productRepository.getAll().first { it.id != product.id }
        val anotherProductAisleProduct =
            aisleProductRepository.getProductAisles(anotherProduct.id).first()
        val aisle = aisleRepository.get(anotherProductAisleProduct.aisleId)!!
        val newAisleForOtherProduct = aisleRepository.getAll()
            .first { it.locationId == aisle.locationId && it.id != aisle.id }
        val originalAisleProducts = aisleProductRepository.getProductAisles(product.id)

        changeProductAisleUseCase(
            product.id,
            anotherProductAisleProduct.aisleId,
            newAisleForOtherProduct.id
        )

        val finalAisleProducts = aisleProductRepository.getProductAisles(product.id)
        assertEquals(originalAisleProducts, finalAisleProducts)
    }

    @Test
    fun changeProductAisle_currentAisleMissing_doesNothing() = runTest {
        val originalAisleProducts = aisleProductRepository.getProductAisles(product.id)
        val aisle = aisleRepository.get(aisleProduct.aisleId)!!
        val newAisle = aisleRepository.getAll()
            .first { it.locationId == aisle.locationId && it.id != aisle.id }
        changeProductAisleUseCase(product.id, -1, newAisle.id)
        val finalAisleProducts = aisleProductRepository.getProductAisles(product.id)
        assertEquals(originalAisleProducts, finalAisleProducts)
    }

    @Test
    fun changeProductAisle_newAisleMissing_doesNothing() = runTest {
        val originalAisleProducts = aisleProductRepository.getProductAisles(product.id)
        changeProductAisleUseCase(product.id, aisleProduct.aisleId, -1)
        val finalAisleProducts = aisleProductRepository.getProductAisles(product.id)
        assertEquals(originalAisleProducts, finalAisleProducts)
    }

    @Test
    fun changeProductAisle_validMove_updatesAisleProduct() = runTest {
        val aisleForProduct = aisleRepository.get(aisleProduct.aisleId)!!
        val newAisle = aisleRepository.getAll()
            .first { it.locationId == aisleForProduct.locationId && it.id != aisleForProduct.id }
        val getAisleProductMaxRankUseCase = dm.getUseCase<GetAisleProductMaxRankUseCase>()
        val newAisleProductMaxRank = getAisleProductMaxRankUseCase(newAisle)

        changeProductAisleUseCase(product.id, aisleProduct.aisleId, newAisle.id)

        val updatedAisleProduct =
            aisleProductRepository.getProductAisles(product.id).first { it.aisleId == newAisle.id }
        assertEquals(newAisle.id, updatedAisleProduct.aisleId)
        assertEquals(newAisleProductMaxRank + 1, updatedAisleProduct.rank)
    }
}
