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

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddNoteUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var addNoteUseCase: AddNoteUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        repository = dm.getRepository<NoteRepository>()
        addNoteUseCase = dm.getUseCase()
    }

    @Test
    fun invoke_NoExistingNote_NoteCreated() = runTest {
        val newItem = Note(
            id = 0,
            noteText = "New Note"
        )

        val countBefore = repository.getAll().count()

        val resultId = addNoteUseCase(newItem)

        val countAfter = repository.getAll().count()
        val addedItem = repository.get(resultId)

        assertEquals(countBefore + 1, countAfter)
        assertEquals(newItem.copy(id = resultId), addedItem)
    }

    @Test
    fun invoke_IsExistingNote_NoteUpdated() = runTest {
        val updatedNote = "Updated Note"
        val id = repository.add(Note(id = 0, noteText = "Existing Note"))
        val existing = repository.get(id)!!

        val resultId = addNoteUseCase(existing.copy(noteText = updatedNote))
        val updated = repository.get(id)

        assertEquals(-1, resultId)
        assertEquals(existing.copy(noteText = updatedNote), updated)
    }
}