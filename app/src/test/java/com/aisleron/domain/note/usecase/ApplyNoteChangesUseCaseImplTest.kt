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
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApplyNoteChangesUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var applyNoteChangesUseCase: ApplyNoteChangesUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setup() {
        dm = TestDependencyManager()
        repository = dm.getRepository()
        applyNoteChangesUseCase = dm.getUseCase()
    }

    private suspend fun getProduct(): Product {
        return dm.getRepository<ProductRepository>().getAll().first()
    }

    @Test
    fun invoke_NoteIsNull_ReturnsNullAndNoActions() = runTest {
        val noted = getProduct()

        val result = applyNoteChangesUseCase(noted, null)

        assertNull(result)
    }

    @Test
    fun invoke_NoteTextIsBlank_RemovesNoteAndReturnsNull() = runTest {
        repository.add(Note(0, "Note 1"))
        val noteId = repository.add(Note(0, "Note 2"))
        val note = repository.get(noteId)!!.copy(noteText = "")
        val countBefore = repository.getAll().count()

        val noted = getProduct().copy(noteId = noteId, note = note)

        val result = applyNoteChangesUseCase(noted, note)

        assertNull(result)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore.dec(), countAfter)
    }

    @Test
    fun invoke_ExistingNoteIdMatchesAndNonZero_UpdatesNoteAndReturnsSameId() = runTest {
        val noteId = repository.add(Note(0, "Note 1"))
        val updatedNoteText = "Updated note"
        val note = repository.get(noteId)!!.copy(noteText = updatedNoteText)
        val noted = getProduct().copy(noteId = noteId, note = note)

        val result = applyNoteChangesUseCase(noted, note)

        assertEquals(noteId, result)

        val updatedNote = repository.get(result!!)
        assertEquals(note, updatedNote)
    }

    @Test
    fun invoke_NoteIdIsZero_AddsNoteAndReturnsNewId() = runTest {
        val noteText = "Add new note"
        val note = Note(id = 0, noteText = noteText)
        val noted = getProduct().copy(noteId = null, note = note)

        val result = applyNoteChangesUseCase(noted, note)

        val addedNote = repository.getAll().firstOrNull { it.noteText == noteText }
        assertEquals(result, addedNote?.id)
        assertEquals(noteText, addedNote?.noteText)
    }

    @Test
    fun invoke_ExistingNoteIdDiffersFromNoteId_AddsNoteAndReturnsNewId() = runTest {
        val noteText = "Add new note"
        val noteId1 = repository.add(Note(0, "Note 1"))
        val noteId2 = repository.add(Note(0, "Note 2"))
        val note = repository.get(noteId1)!!.copy(noteText = noteText)
        val noted = getProduct().copy(noteId = noteId2, note = note)

        val result = applyNoteChangesUseCase(noted, note)

        assertNotEquals(noteId1, result)
        assertNotEquals(noteId2, result)

        val addedNote = repository.getAll().firstOrNull { it.noteText == noteText }
        assertEquals(result, addedNote?.id)
    }
}
