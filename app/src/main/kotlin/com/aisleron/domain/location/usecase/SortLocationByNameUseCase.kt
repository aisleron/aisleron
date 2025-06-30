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

package com.aisleron.domain.location.usecase

import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.flow.firstOrNull

interface SortLocationByNameUseCase {
    suspend operator fun invoke(locationId: Int)
}

class SortLocationByNameUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val updateAisleUseCase: UpdateAisleUseCase,
    private val updateAisleProductUseCase: UpdateAisleProductsUseCase
) : SortLocationByNameUseCase {
    override suspend operator fun invoke(locationId: Int) {
        val loc = locationRepository.getLocationWithAislesWithProducts(locationId).firstOrNull()
        loc?.let { location ->
            location.aisles
                .map { aisle ->
                    val sortedAisleProducts = aisle.products
                        .sortedBy { it.product.name.lowercase() }
                        .mapIndexed { index, aisleProduct ->
                            aisleProduct.rank = index + 1
                            aisleProduct
                        }
                    updateAisleProductUseCase(aisle.products)
                    aisle.copy(products = sortedAisleProducts)
                }
                .sortedWith(compareBy({ it.isDefault }, { it.name.lowercase() }))
                .mapIndexed { index, aisle ->
                    val updatedAisle = aisle.copy(rank = index + 1)
                    updateAisleUseCase(updatedAisle)
                }
        }
    }
}
