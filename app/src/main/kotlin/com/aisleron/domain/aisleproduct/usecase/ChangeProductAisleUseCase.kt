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

package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException

interface ChangeProductAisleUseCase {
    suspend operator fun invoke(productId: Int, currentAisleId: Int, newAisleId: Int)
}

class ChangeProductAisleUseCaseImpl(
    private val aisleProductRepository: AisleProductRepository,
    private val getAisleUseCase: GetAisleUseCase,
    private val getAisleProductMaxRankUseCase: GetAisleProductMaxRankUseCase,
    private val updateAisleProductUseCase: UpdateAisleProductsUseCase
) : ChangeProductAisleUseCase {
    override suspend fun invoke(
        productId: Int, currentAisleId: Int, newAisleId: Int
    ) {
        if (currentAisleId == newAisleId) return
        val currentAisle = getAisleUseCase(currentAisleId) ?: return
        val newAisle = getAisleUseCase(newAisleId) ?: return

        if (currentAisle.locationId != newAisle.locationId) {
            throw AisleronException.AisleMoveException("Aisle move must be within the same Location.")
        }

        val aisleProductToMove = aisleProductRepository.getProductAisles(productId).singleOrNull {
            it.aisleId == currentAisle.id
        }

        aisleProductToMove?.let {
            val aisleProductMaxRank = getAisleProductMaxRankUseCase(newAisle)
            val updatedAisleProduct = it.copy(
                rank = aisleProductMaxRank.inc(),
                aisleId = newAisle.id
            )
            updateAisleProductUseCase(listOf(updatedAisleProduct))
        }
    }
}