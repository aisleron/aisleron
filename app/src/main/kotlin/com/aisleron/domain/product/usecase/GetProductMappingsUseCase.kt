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

import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.GetLocationUseCase

interface GetProductMappingsUseCase {
    suspend operator fun invoke(productId: Int): List<Location>
}

class GetProductMappingsUseCaseImpl(
    private val aisleProductRepository: AisleProductRepository,
    private val getAisleUseCase: GetAisleUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val getDefaultAislesUseCase: GetDefaultAislesUseCase
) : GetProductMappingsUseCase {
    override suspend fun invoke(productId: Int): List<Location> {
        val mappedAislesByLocation = aisleProductRepository.getProductAisles(productId)
            .mapNotNull { getAisleUseCase(it.aisleId) }
            .groupBy { it.locationId }

        val defaultAislesByLocation = getDefaultAislesUseCase().groupBy { it.locationId }

        // 1. Start with a map of default Location ID -> Aisle List
        val finalAisles = defaultAislesByLocation.toMutableMap()

        // 2. Overwrite defaults with mapped aisles (prioritization)
        finalAisles.putAll(mappedAislesByLocation)

        // 3. Convert the map into the final list of Location objects
        return finalAisles.mapNotNull { (locationId, aisles) ->
            getLocationUseCase(locationId)?.copy(aisles = aisles)
        }
    }
}
