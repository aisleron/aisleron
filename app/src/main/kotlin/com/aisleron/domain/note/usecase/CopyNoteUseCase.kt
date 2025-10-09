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

interface CopyNoteUseCase {
    suspend operator fun invoke(noteId: Int?): Int?
}

class CopyNoteUseCaseImpl(
    private val addNoteUseCase: AddNoteUseCase,
    private val getNoteUseCase: GetNoteUseCase
) : CopyNoteUseCase {
    override suspend fun invoke(noteId: Int?): Int? {
        noteId ?: return null

        val source = getNoteUseCase(noteId)
        return source?.let { addNoteUseCase(it.copy(id = 0)) }
    }
}