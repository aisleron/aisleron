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

interface ApplyNoteChangesUseCase {
    suspend operator fun invoke(item: NoteParent, note: Note?): Int?
}

class ApplyNoteChangesUseCaseImpl(
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val removeNoteUseCase: RemoveNoteUseCase
) : ApplyNoteChangesUseCase {
    override suspend fun invoke(item: NoteParent, note: Note?): Int? {
        note ?: return null

        return when {
            note.noteText.isBlank() -> {
                removeNoteUseCase(item, note)
                null
            }

            item.noteId == note.id && note.id != 0 -> {
                updateNoteUseCase(note)
                note.id
            }

            else -> {
                addNoteUseCase(item, Note(id = 0, noteText = note.noteText))
            }
        }
    }
}