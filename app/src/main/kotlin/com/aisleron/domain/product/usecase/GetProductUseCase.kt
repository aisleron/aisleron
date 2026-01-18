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

package com.aisleron.domain.product.usecase

import com.aisleron.domain.base.usecase.GetUseCase
import com.aisleron.domain.note.usecase.GetNoteUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

interface GetProductUseCase : GetUseCase<Product>

class GetProductUseCaseImpl(
    private val productRepository: ProductRepository,
    private val getNoteUseCase: GetNoteUseCase
) : GetProductUseCase {
    override suspend operator fun invoke(id: Int): Product? {
        val product = productRepository.get(id)
        val note = product?.noteId?.let { getNoteUseCase(it) }

        return product?.copy(note = note)
    }
}