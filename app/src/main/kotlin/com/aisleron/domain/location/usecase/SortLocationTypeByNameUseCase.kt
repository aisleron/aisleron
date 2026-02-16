/*
 * Copyright (C) 2025-2026 aisleron.com
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

import com.aisleron.domain.TransactionRunner
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType

interface SortLocationTypeByNameUseCase {
    suspend operator fun invoke(locationType: LocationType, sortAisles: Boolean)
}

class SortLocationTypeByNameUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val sortLocationByNameUseCase: SortLocationByNameUseCase,
    private val transactionRunner: TransactionRunner
) : SortLocationTypeByNameUseCase {
    override suspend operator fun invoke(locationType: LocationType, sortAisles: Boolean) {
        val locations = locationRepository.getByType(locationType)
            .sortedBy { it.name.lowercase() }

        if (locations.isEmpty()) return

        transactionRunner.run {
            val updatedLocations = locations.mapIndexed { index, location ->
                location.copy(rank = index + 1)
            }

            locationRepository.update(updatedLocations)

            if (sortAisles) {
                updatedLocations.forEach { location ->
                    sortLocationByNameUseCase(location.id)
                }
            }
        }
    }
}
