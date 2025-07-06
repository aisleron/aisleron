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

package com.aisleron.di

import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.aisle.AisleRepositoryImpl
import com.aisleron.data.aisleproduct.AisleProductRankMapper
import com.aisleron.data.aisleproduct.AisleProductRepositoryImpl
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.data.loyaltycard.LoyaltyCardMapper
import com.aisleron.data.loyaltycard.LoyaltyCardRepositoryImpl
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.domain.product.ProductRepository
import org.koin.dsl.module

val repositoryModule = module {
    factory<LocationRepository> {
        LocationRepositoryImpl(locationDao = get(), locationMapper = LocationMapper())
    }

    factory<AisleRepository> {
        AisleRepositoryImpl(
            aisleDao = get(), aisleMapper = AisleMapper()
        )
    }

    factory<AisleProductRepository> {
        AisleProductRepositoryImpl(
            aisleProductDao = get(), aisleProductRankMapper = AisleProductRankMapper()
        )
    }

    factory<ProductRepository> {
        ProductRepositoryImpl(
            productDao = get(), aisleProductDao = get(), productMapper = ProductMapper()
        )
    }

    factory<LoyaltyCardRepository> {
        LoyaltyCardRepositoryImpl(
            loyaltyCardDao = get(),
            locationLoyaltyCardDao = get(),
            loyaltyCardMapper = LoyaltyCardMapper()
        )
    }
}