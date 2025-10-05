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

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.base.usecase.UpdateUseCase
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.usecase.AddNoteUseCase
import com.aisleron.domain.note.usecase.RemoveNoteUseCase
import com.aisleron.domain.note.usecase.UpdateNoteUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

interface UpdateProductUseCase : UpdateUseCase<Product>

class UpdateProductUseCaseImpl(
    private val productRepository: ProductRepository,
    private val isProductNameUniqueUseCase: IsProductNameUniqueUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val removeNoteUseCase: RemoveNoteUseCase
) : UpdateProductUseCase {
    override suspend operator fun invoke(item: Product) {

        if (!isProductNameUniqueUseCase(item)) {
            throw AisleronException.DuplicateProductNameException("Product Name must be unique")
        }

        val noteId = handleNoteUpdate(item)
        productRepository.update(item.copy(noteId = noteId))
    }

    private suspend fun handleNoteUpdate(product: Product): Int? {
        val note = product.note ?: return product.noteId

        return when {
            note.noteText.isBlank() -> {
                removeNoteUseCase(note)
                null
            }

            product.noteId == null || product.noteId == 0 || product.noteId != note.id || note.id == 0 -> {
                addNoteUseCase(Note(id = 0, noteText = note.noteText))
            }

            else -> {
                updateNoteUseCase(note)
                note.id
            }
        }
    }
}
