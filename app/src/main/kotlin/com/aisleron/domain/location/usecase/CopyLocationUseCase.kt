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

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.flow.first

interface CopyLocationUseCase {
    suspend operator fun invoke(source: Location, newLocationName: String): Int
}

class CopyLocationUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val aisleRepository: AisleRepository,
    private val aisleProductRepository: AisleProductRepository,
    private val isLocationNameUniqueUseCase: IsLocationNameUniqueUseCase
) : CopyLocationUseCase {
    override suspend fun invoke(source: Location, newLocationName: String): Int {
        val newLocation = source.copy(
            id = 0,
            name = newLocationName,
            aisles = emptyList()
        )

        if (!isLocationNameUniqueUseCase(newLocation)) {
            throw AisleronException.DuplicateLocationNameException("Location Name must be unique")
        }

        val sourceShoppingList = locationRepository.getLocationWithAislesWithProducts(source.id).first()
        return locationRepository.add(newLocation).let { newLocationId ->
            sourceShoppingList?.aisles?.forEach { aisle ->
                copyAisle(aisle, newLocationId)
            }

            newLocationId
        }
    }

    private suspend fun copyAisle(sourceAisle: Aisle, locationId: Int) =
        aisleRepository.add(
            sourceAisle.copy(
                id = 0,
                locationId = locationId,
                products = emptyList()
            )
        ).let { newAisleId ->
            sourceAisle.products.forEach { ap ->
                copyAisleProduct(ap, newAisleId)
            }
        }

    private suspend fun copyAisleProduct(sourceAisleProduct: AisleProduct, aisleId: Int) =
        aisleProductRepository.add(
            sourceAisleProduct.copy(
                id = 0,
                aisleId = aisleId
            )
        )
}