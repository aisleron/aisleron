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

import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType

interface ExpandCollapseLocationsUseCase {
    suspend operator fun invoke(locationType: LocationType, expand: Boolean)
}

class ExpandCollapseLocationsUseCaseImpl(
    private val locationRepository: LocationRepository
) : ExpandCollapseLocationsUseCase {
    override suspend operator fun invoke(locationType: LocationType, expand: Boolean) {
        val locations = locationRepository.getByType(locationType)
        val updatedLocations = locations.filter { it.expanded != expand }
            .map { it.copy(expanded = expand) }

        if (updatedLocations.isEmpty()) return

        locationRepository.update(updatedLocations)
    }
}