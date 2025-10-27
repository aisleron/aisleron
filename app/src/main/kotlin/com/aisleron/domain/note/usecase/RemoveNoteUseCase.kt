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

import com.aisleron.domain.TransactionRunner
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteParent
import com.aisleron.domain.note.NoteRepository

interface RemoveNoteUseCase {
    suspend operator fun invoke(noteParent: NoteParent, note: Note)
    suspend operator fun invoke(noteParent: NoteParent, noteId: Int)
}

class RemoveNoteUseCaseImpl(
    private val noteRepository: NoteRepository,
    private val removeNoteFromParentUseCase: RemoveNoteFromParentUseCase,
    private val transactionRunner: TransactionRunner
) : RemoveNoteUseCase {
    override suspend fun invoke(noteParent: NoteParent, note: Note) {
        if (noteParent.noteId != note.id) return

        transactionRunner.run {
            removeNoteFromParentUseCase(noteParent, note.id)
            noteRepository.remove(note)
        }
    }

    override suspend fun invoke(noteParent: NoteParent, noteId: Int) {
        val note = noteRepository.get(noteId)
        note?.let { invoke(noteParent, it) }
    }
}