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

package com.aisleron.domain.note.usecase

import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteParent
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.ui.note.NoteParentRef

interface GetNoteParentUseCase {
    suspend operator fun invoke(noteParentRef: NoteParentRef): NoteParent?
}

class GetNoteParentUseCaseImpl(
    private val getProductUseCase: GetProductUseCase,
    private val getNoteUseCase: GetNoteUseCase
) : GetNoteParentUseCase {
    override suspend fun invoke(noteParentRef: NoteParentRef): NoteParent? {
        return when (noteParentRef) {
            is NoteParentRef.Product -> getProductNoteParent(noteParentRef.id)
        }
    }

    private suspend fun getNote(noteId: Int): Note? {
        return getNoteUseCase(noteId)
    }

    private suspend fun getProductNoteParent(productId: Int): NoteParent? {
        return getProductUseCase(productId)?.let { p ->
            p.noteId?.let { noteId -> p.copy(note = getNote(noteId)) } ?: p
        }
    }
}