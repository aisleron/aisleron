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

import com.aisleron.data.AisleronDb
import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.aisle.AisleRepositoryImpl
import com.aisleron.data.aisleproduct.AisleProductRankMapper
import com.aisleron.data.aisleproduct.AisleProductRepositoryImpl
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.data.loyaltycard.LoyaltyCardMapper
import com.aisleron.data.loyaltycard.LoyaltyCardRepositoryImpl
import com.aisleron.data.note.NoteMapper
import com.aisleron.data.note.NoteRepositoryImpl
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.ProductRepository

class TestRepositoryFactory(private val db: AisleronDb) {
    val aisleRepository: AisleRepository by lazy {
        AisleRepositoryImpl(db.aisleDao(), AisleMapper())
    }

    val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(
            db.productDao(),
            db.aisleProductDao(),
            ProductMapper()
        )
    }

    val aisleProductRepository: AisleProductRepository by lazy {
        AisleProductRepositoryImpl(
            db.aisleProductDao(), AisleProductRankMapper()
        )
    }

    val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(db.locationDao(), LocationMapper())
    }

    val loyaltyCardRepository: LoyaltyCardRepository by lazy {
        LoyaltyCardRepositoryImpl(
            db.loyaltyCardDao(), db.locationLoyaltyCardDao(), LoyaltyCardMapper()
        )
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(db.noteDao(), NoteMapper())
    }

    inline fun <reified T> get(): T {
        return when (T::class) {
            AisleRepository::class -> aisleRepository as T
            ProductRepository::class -> productRepository as T
            AisleProductRepository::class -> aisleProductRepository as T
            LocationRepository::class -> locationRepository as T
            LoyaltyCardRepository::class -> loyaltyCardRepository as T
            NoteRepository::class -> noteRepository as T

            else -> throw Exception("Unknown repository ${T::class}")
        }
    }
}