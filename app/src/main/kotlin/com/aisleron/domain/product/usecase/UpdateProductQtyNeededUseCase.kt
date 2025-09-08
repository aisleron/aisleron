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

import com.aisleron.domain.product.Product

interface UpdateProductQtyNeededUseCase {
    suspend operator fun invoke(id: Int, quantity: Int): Product?
}

class UpdateProductQtyNeededUseCaseImpl(
    private val getProductUseCase: GetProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : UpdateProductQtyNeededUseCase {
    override suspend operator fun invoke(id: Int, quantity: Int): Product? {
        require(quantity >= 0)

        val product = getProductUseCase(id)?.copy(qtyNeeded = quantity)
        if (product != null) {
            updateProductUseCase(product)
        }

        return product
    }
}