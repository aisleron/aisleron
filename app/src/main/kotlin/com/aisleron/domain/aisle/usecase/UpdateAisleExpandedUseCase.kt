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

package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

interface UpdateAisleExpandedUseCase {
    suspend operator fun invoke(aisle: Aisle, expanded: Boolean)
    suspend operator fun invoke(aisleId: Int, expanded: Boolean)
}

class UpdateAisleExpandedUseCaseImpl(
    private val aisleRepository: AisleRepository,
    private val updateAisleUseCase: UpdateAisleUseCase
) : UpdateAisleExpandedUseCase {
    override suspend operator fun invoke(aisle: Aisle, expanded: Boolean) {
        val updatedAisle = aisle.copy(expanded = expanded)
        updateAisleUseCase(updatedAisle)
    }

    override suspend fun invoke(aisleId: Int, expanded: Boolean) {
        val aisle = aisleRepository.get(aisleId)
        aisle?.let { invoke(it, expanded) }
    }
}