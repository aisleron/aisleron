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

package com.aisleron.domain.product.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetProductMappingsUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getProductMappingsUseCase: GetProductMappingsUseCase
    private lateinit var productRepository: ProductRepository
    private lateinit var aisleProductRepository: AisleProductRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var aisleRepository: AisleRepository
    private lateinit var product: Product

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getProductMappingsUseCase = dm.getUseCase()
        productRepository = dm.getRepository()
        aisleProductRepository = dm.getRepository()
        locationRepository = dm.getRepository()
        aisleRepository = dm.getRepository()
        runBlocking {
            val newProductId = productRepository.add(Product(0, "New Product", false, 0))
            product = productRepository.get(newProductId)!!
        }
    }

    @Test
    fun getProductMappings_productNotMapped_returnsEmptyList() = runTest {
        val result = getProductMappingsUseCase(product.id)

        assertTrue(result.isEmpty())
    }

    @Test
    fun getProductMappings_productMappedToOneAisle_returnsLocationWithAisle() = runTest {
        val location = locationRepository.getAll().first()
        val aisle = aisleRepository.getForLocation(location.id).first()
        aisleProductRepository.add(
            AisleProduct(1, aisle.id, product, 0)
        )

        val result = getProductMappingsUseCase(product.id)

        assertEquals(1, result.size)
        assertEquals(location.id, result.first().id)
        assertEquals(1, result.first().aisles.size)
        assertEquals(aisle.id, result.first().aisles.single().id)
    }

    @Test
    fun getProductMappings_productMappedToMultipleAisles_returnsMultipleLocations() = runTest {
        val location1 = locationRepository.getAll().first()
        val aisle1 = aisleRepository.getForLocation(location1.id).first()
        aisleProductRepository.add(
            AisleProduct(1, aisle1.id, product, -1)
        )

        val location2 = locationRepository.getAll().first { it.id != location1.id }
        val aisle2 = aisleRepository.getForLocation(location2.id).first()
        aisleProductRepository.add(
            AisleProduct(1, aisle2.id, product, -2)
        )

        val result = getProductMappingsUseCase(product.id)

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == location1.id && it.aisles.single().id == aisle1.id })
        assertTrue(result.any { it.id == location2.id && it.aisles.single().id == aisle2.id })
    }

    @Test
    fun getProductMappings_aisleOrLocationMissing_areIgnored() = runTest {
        val location = locationRepository.getAll().first()
        val aisle = aisleRepository.getForLocation(location.id).first()
        aisleProductRepository.add(
            AisleProduct(1, aisle.id, product, 0)
        )

        aisleRepository.remove(aisle) // Aisle is now missing

        val result = getProductMappingsUseCase(product.id)

        assertTrue(result.isEmpty())
    }
}
