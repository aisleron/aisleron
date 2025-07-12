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

package com.aisleron.data

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.aisle.AisleRepositoryImpl
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.aisleproduct.AisleProductRankMapper
import com.aisleron.data.aisleproduct.AisleProductRepositoryImpl
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardMapper
import com.aisleron.data.loyaltycard.LoyaltyCardRepositoryImpl
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.GetCreateSampleDataUseCase
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.domain.product.ProductRepository
import com.aisleron.testdata.data.aisle.AisleDaoTestImpl
import com.aisleron.testdata.data.aisleproduct.AisleProductDaoTestImpl
import com.aisleron.testdata.data.location.LocationDaoTestImpl
import com.aisleron.testdata.data.loyaltycard.LocationLoyaltyCardDaoTestImpl
import com.aisleron.testdata.data.loyaltycard.LoyaltyCardDaoTestImpl
import com.aisleron.testdata.data.product.ProductDaoTestImpl
import kotlinx.coroutines.runBlocking

class TestDataManager(private val addData: Boolean = true) {

    private val _productDao = ProductDaoTestImpl()
    private val _aisleProductDao = AisleProductDaoTestImpl(_productDao)
    private val _aisleDao = AisleDaoTestImpl(_aisleProductDao)
    private val _locationDao = LocationDaoTestImpl(_aisleDao)
    private val _locationLoyaltyCardDao = LocationLoyaltyCardDaoTestImpl()
    private val _loyaltyCardDao = LoyaltyCardDaoTestImpl(_locationLoyaltyCardDao)

    fun aisleDao(): AisleDao = _aisleDao

    fun locationDao(): LocationDao = _locationDao

    fun productDao(): ProductDao = _productDao

    fun aisleProductDao(): AisleProductDao = _aisleProductDao

    fun loyaltyCardDao(): LoyaltyCardDao = _loyaltyCardDao

    fun locationLoyaltyCardDao(): LocationLoyaltyCardDao = _locationLoyaltyCardDao

    init {
        initializeTestData()
    }

    private fun initializeTestData() {
        runBlocking {
            DbInitializer(_locationDao, _aisleDao, this).invoke()
        }

        if (addData) {
            val createSampleDataUseCase = GetCreateSampleDataUseCase().invoke(
                getRepository<LocationRepository>(),
                getRepository<AisleRepository>(),
                getRepository<ProductRepository>(),
                getRepository<AisleProductRepository>(),
            )
            runBlocking { createSampleDataUseCase() }
        }
    }

    inline fun <reified T> getRepository(): T {
        return when (T::class) {
            AisleRepository::class -> AisleRepositoryImpl(aisleDao(), AisleMapper()) as T
            ProductRepository::class -> ProductRepositoryImpl(
                productDao(), aisleProductDao(), ProductMapper()
            ) as T

            AisleProductRepository::class -> AisleProductRepositoryImpl(
                aisleProductDao(), AisleProductRankMapper()
            ) as T

            LocationRepository::class -> LocationRepositoryImpl(
                locationDao(), LocationMapper()
            ) as T

            LoyaltyCardRepository::class -> LoyaltyCardRepositoryImpl(
                loyaltyCardDao(), locationLoyaltyCardDao(), LoyaltyCardMapper()
            ) as T

            else -> throw Exception("Invalid repository type requested")
        }
    }
}