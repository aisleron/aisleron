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
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.usecase.GetAllProductsUseCase

interface AddLocationUseCase {
    suspend operator fun invoke(location: Location): Int
}

class AddLocationUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val addAisleUseCase: AddAisleUseCase,
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val addAisleProductsUseCase: AddAisleProductsUseCase,
    private val isLocationNameUniqueUseCase: IsLocationNameUniqueUseCase

) : AddLocationUseCase {
    override suspend operator fun invoke(location: Location): Int {

        if (!isLocationNameUniqueUseCase(location)) {
            throw AisleronException.DuplicateLocationNameException("Location Name must be unique")
        }

        if (locationRepository.get(location.id) != null) {
            throw AisleronException.DuplicateLocationException("Cannot add a duplicate of an existing Location")
        }

        val newLocationId = locationRepository.add(location)
        //Add location default Aisle. Set Rank high so it shows at the end of the shopping list
        val newAisleId = addAisleUseCase(
            Aisle(
                id = 0,
                name = "No Aisle",
                locationId = newLocationId,
                rank = 1000,
                isDefault = true,
                products = emptyList(),
                expanded = true
            )
        )

        addAisleProductsUseCase(
            getAllProductsUseCase().sortedBy { it.name }.mapIndexed { _, p ->
                AisleProduct(
                    rank = 0,
                    aisleId = newAisleId,
                    product = p,
                    id = 0
                )
            })

        return newLocationId
    }
}