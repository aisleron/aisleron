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
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.base.AisleronException

interface RemoveAisleUseCase {
    suspend operator fun invoke(aisle: Aisle)
}

class RemoveAisleUseCaseImpl(
    private val aisleRepository: AisleRepository,
    private val updateAisleProductsUseCase: UpdateAisleProductsUseCase,
    private val removeProductsFromAisleUseCase: RemoveProductsFromAisleUseCase
) : RemoveAisleUseCase {
    override suspend operator fun invoke(aisle: Aisle) {
        if (aisle.isDefault) {
            throw AisleronException.DeleteDefaultAisleException("Cannot delete default Aisle")
        }

        val aisleWithProducts = aisleRepository.getWithProducts(aisle.id)

        val defaultAisle = aisleRepository.getDefaultAisleFor(aisleWithProducts.locationId)
        if (defaultAisle != null) {
            val aisleProducts = aisleWithProducts.products
            aisleProducts.forEach { it.aisleId = defaultAisle.id }
            updateAisleProductsUseCase(aisleProducts)
        } else {
            removeProductsFromAisleUseCase(aisleWithProducts)
        }
        aisleRepository.remove(aisleWithProducts)
    }
}
