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

package com.aisleron.domain.sampledata.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.shoppinglist.ShoppingListFilter
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateSampleDataUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var createSampleDataUseCase: CreateSampleDataUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager(false)
        createSampleDataUseCase = dm.getUseCase()
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_ProductsCreated() = runTest {
        val productRepository = dm.getRepository<ProductRepository>()
        val productCountBefore = productRepository.getAll().count()

        createSampleDataUseCase()

        val productCountAfter = productRepository.getAll().count()

        Assertions.assertEquals(productCountBefore, 0)
        Assertions.assertTrue(productCountBefore < productCountAfter)
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_HomeAislesCreated() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val homeId = dm.getRepository<LocationRepository>().getHome().id
        val aisleCountBefore = aisleRepository.getAll().count { it.locationId == homeId }

        createSampleDataUseCase()

        val aisleCountAfter = aisleRepository.getAll().count { it.locationId == homeId }

        Assertions.assertEquals(aisleCountBefore, 1)
        Assertions.assertTrue(aisleCountBefore < aisleCountAfter)
    }

    @Test
    fun createSampleDataUseCase_HomeAislesCreated_ProductsMappedInHomeAisles() = runTest {
        createSampleDataUseCase()

        val locationRepository = dm.getRepository<LocationRepository>()
        val homeId = locationRepository.getHome().id
        val homeList =
            dm.getUseCase<GetShoppingListUseCase>().invoke(homeId, ShoppingListFilter()).first()!!

        val aisleProductCountAfter = homeList.aisles.find { !it.isDefault }?.products?.count() ?: 0

        Assertions.assertTrue(0 < aisleProductCountAfter)
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_ShopCreated() = runTest {
        val locationRepository = dm.getRepository<LocationRepository>()
        val shopCountBefore = locationRepository.getShops().first().count()

        createSampleDataUseCase()

        val shopCountAfter = locationRepository.getShops().first().count()

        Assertions.assertEquals(shopCountBefore, 0)
        Assertions.assertTrue(shopCountBefore < shopCountAfter)
    }

    @Test
    fun createSampleDataUseCase_ShopCreated_ProductsMappedInShopAisles() = runTest {
        createSampleDataUseCase()

        val locationRepository = dm.getRepository<LocationRepository>()
        val shopId = locationRepository.getShops().first().first().id
        val shopList =
            dm.getUseCase<GetShoppingListUseCase>().invoke(shopId, ShoppingListFilter()).first()!!

        val aisleProductCountAfter = shopList.aisles.find { !it.isDefault }?.products?.count() ?: 0

        Assertions.assertTrue(0 < aisleProductCountAfter)
    }

    @Test
    fun createSampleDataUseCase_ProductsExistInDatabase_ThrowsException() = runTest {
        val addProductUseCase = dm.getUseCase<AddProductUseCase>()
        addProductUseCase(
            Product(
                id = 0,
                name = "CreateSampleDataProductExistsTest",
                inStock = false,
                qtyNeeded = 0.0,
                noteId = null,
                qtyIncrement = 1.0,
                trackingMode = TrackingMode.DEFAULT,
                unitOfMeasure = "Qty"
            )
        )

        assertThrows<AisleronException.SampleDataCreationException> {
            createSampleDataUseCase()
        }
    }

}
