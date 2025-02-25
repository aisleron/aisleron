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
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

interface RemoveLocationUseCase {
    suspend operator fun invoke(location: Location)
}

class RemoveLocationUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val removeAisleUseCase: RemoveAisleUseCase,
    private val removeDefaultAisleUseCase: RemoveDefaultAisleUseCase
) : RemoveLocationUseCase {
    override suspend operator fun invoke(location: Location) {
        val loc = locationRepository.getLocationWithAisles(location.id)
        val aisles = loc.aisles.filter { !it.isDefault }
        aisles.forEach { removeAisleUseCase(it) }

        val defaultAisle: Aisle? = loc.aisles.firstOrNull { it.isDefault }
        defaultAisle?.let {
            removeDefaultAisleUseCase(defaultAisle)
        }

        locationRepository.remove(loc)
    }
}
