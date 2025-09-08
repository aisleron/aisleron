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

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisle.usecase.IsAisleNameUniqueUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.GetAisleMaxRankUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.AddLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateSampleDataUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var createSampleDataUseCase: CreateSampleDataUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager(false)

        val locationRepository = testData.getRepository<LocationRepository>()
        val aisleRepository = testData.getRepository<AisleRepository>()
        val productRepository = testData.getRepository<ProductRepository>()
        val aisleProductRepository = testData.getRepository<AisleProductRepository>()

        val getShoppingListUseCase = GetShoppingListUseCase(locationRepository)
        val getAllProductsUseCase = GetAllProductsUseCase(productRepository)
        val getHomeLocationUseCase = GetHomeLocationUseCase(locationRepository)
        val addAisleProductsUseCase = AddAisleProductsUseCase(aisleProductRepository)
        val updateAisleProductRankUseCase = UpdateAisleProductRankUseCase(aisleProductRepository)

        val addProductUseCase = AddProductUseCaseImpl(
            productRepository,
            GetDefaultAislesUseCase(aisleRepository),
            addAisleProductsUseCase,
            IsProductNameUniqueUseCase(productRepository),
            GetAisleMaxRankUseCase(aisleProductRepository)
        )

        val addAisleUseCase = AddAisleUseCaseImpl(
            aisleRepository,
            GetLocationUseCase(locationRepository),
            IsAisleNameUniqueUseCase(aisleRepository)
        )

        val addLocationUseCase = AddLocationUseCaseImpl(
            locationRepository,
            addAisleUseCase,
            getAllProductsUseCase,
            addAisleProductsUseCase,
            IsLocationNameUniqueUseCase(locationRepository)
        )

        createSampleDataUseCase = CreateSampleDataUseCaseImpl(
            addProductUseCase = addProductUseCase,
            addAisleUseCase = addAisleUseCase,
            getShoppingListUseCase = getShoppingListUseCase,
            updateAisleProductRankUseCase = updateAisleProductRankUseCase,
            addLocationUseCase = addLocationUseCase,
            getAllProductsUseCase = getAllProductsUseCase,
            getHomeLocationUseCase = getHomeLocationUseCase
        )
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_ProductsCreated() {
        val productRepository = testData.getRepository<ProductRepository>()
        val productCountBefore = runBlocking { productRepository.getAll().count() }

        runBlocking { createSampleDataUseCase() }

        val productCountAfter = runBlocking { productRepository.getAll().count() }

        Assertions.assertEquals(productCountBefore, 0)
        Assertions.assertTrue(productCountBefore < productCountAfter)
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_HomeAislesCreated() {
        val aisleRepository = testData.getRepository<AisleRepository>()
        val homeId = runBlocking { testData.getRepository<LocationRepository>().getHome().id }
        val aisleCountBefore =
            runBlocking { aisleRepository.getAll().count { it.locationId == homeId } }

        runBlocking { createSampleDataUseCase() }

        val aisleCountAfter =
            runBlocking { aisleRepository.getAll().count { it.locationId == homeId } }

        Assertions.assertEquals(aisleCountBefore, 1)
        Assertions.assertTrue(aisleCountBefore < aisleCountAfter)
    }

    @Test
    fun createSampleDataUseCase_HomeAislesCreated_ProductsMappedInHomeAisles() {
        runBlocking { createSampleDataUseCase() }

        val homeList = runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            val homeId = locationRepository.getHome().id
            GetShoppingListUseCase(locationRepository).invoke(homeId).first()!!
        }

        val aisleProductCountAfter = homeList.aisles.find { !it.isDefault }?.products?.count() ?: 0

        Assertions.assertTrue(0 < aisleProductCountAfter)
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_ShopCreated() {
        val locationRepository = testData.getRepository<LocationRepository>()
        val shopCountBefore = runBlocking { locationRepository.getShops().first().count() }

        runBlocking { createSampleDataUseCase() }

        val shopCountAfter = runBlocking { locationRepository.getShops().first().count() }

        Assertions.assertEquals(shopCountBefore, 0)
        Assertions.assertTrue(shopCountBefore < shopCountAfter)
    }

    @Test
    fun createSampleDataUseCase_ShopCreated_ProductsMappedInShopAisles() {
        runBlocking { createSampleDataUseCase() }

        val shopList = runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            val shopId = locationRepository.getShops().first().first().id
            GetShoppingListUseCase(locationRepository).invoke(shopId).first()!!
        }

        val aisleProductCountAfter = shopList.aisles.find { !it.isDefault }?.products?.count() ?: 0

        Assertions.assertTrue(0 < aisleProductCountAfter)
    }

    @Test
    fun createSampleDataUseCase_ProductsExistInDatabase_ThrowsException() {
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            val addProductUseCase = AddProductUseCaseImpl(
                productRepository,
                GetDefaultAislesUseCase(testData.getRepository<AisleRepository>()),
                AddAisleProductsUseCase(aisleProductRepository),
                IsProductNameUniqueUseCase(productRepository),
                GetAisleMaxRankUseCase(aisleProductRepository)
            )

            addProductUseCase(
                Product(
                    id = 0,
                    name = "CreateSampleDataProductExistsTest",
                    inStock = false,
                    qtyNeeded = 0
                ),
                null
            )

            assertThrows<AisleronException.SampleDataCreationException> {
                createSampleDataUseCase()
            }
        }
    }

}
