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

package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

class IsAisleNameUniqueUseCase(private val aisleRepository: AisleRepository) {
    suspend operator fun invoke(aisle: Aisle): Boolean {
        val aisleName = formatName(aisle.name)
        val existingAisle: Aisle? =
            aisleRepository.getForLocation(aisle.locationId)
                .firstOrNull { formatName(it.name) == aisleName }

        //Aisle name is unique if no existing aisle in the same store was found, or
        // the existing aisle has the same id
        return existingAisle?.let { it.id == aisle.id } ?: true
    }

    private fun formatName(name: String) = name.trim().uppercase()
}