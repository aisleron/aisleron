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

import com.aisleron.domain.aisle.AisleRepository

interface ExpandCollapseAislesForLocationUseCase {
    suspend operator fun invoke(locationId: Int)
}

class ExpandCollapseAislesForLocationUseCaseImpl(
    private val aisleRepository: AisleRepository
) : ExpandCollapseAislesForLocationUseCase {
    override suspend operator fun invoke(locationId: Int) {
        val aisles = aisleRepository.getForLocation(locationId)
        val expand = aisles.none { it.expanded }

        val updatedAisles = aisles.filter { it.expanded != expand }
            .map { it.copy(expanded = expand) }

        if (updatedAisles.isEmpty()) return

        aisleRepository.update(updatedAisles)
    }
}