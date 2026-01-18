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

package com.aisleron.domain.note.usecase

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.note.NoteParent
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.UpdateProductUseCase

interface RemoveNoteFromParentUseCase {
    suspend operator fun invoke(item: NoteParent, noteId: Int)
}

class RemoveNoteFromParentUseCaseImpl(
    private val updateProductUseCase: UpdateProductUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase
) : RemoveNoteFromParentUseCase {
    override suspend fun invoke(item: NoteParent, noteId: Int) {
        if (item.noteId != noteId) return

        when (item) {
            is Product -> updateProductUseCase(item.copy(noteId = null, note = null))
            is Location -> updateLocationUseCase(item.copy(noteId = null, note = null))
        }
    }
}