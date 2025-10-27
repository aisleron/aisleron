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

interface AddNoteUseCase {
    suspend operator fun invoke(noteParent: NoteParent, item: Note): Int
}

class AddNoteUseCaseImpl(
    private val noteRepository: NoteRepository,
    private val addNoteToParentUseCase: AddNoteToParentUseCase,
    private val transactionRunner: TransactionRunner
) : AddNoteUseCase {
    override suspend fun invoke(noteParent: NoteParent, item: Note): Int {
        return transactionRunner.run {
            val noteId = noteRepository.add(item)
            addNoteToParentUseCase(noteParent, noteId)
            noteId
        }
    }
}