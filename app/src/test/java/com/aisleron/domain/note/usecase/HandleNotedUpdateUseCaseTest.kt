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
import com.aisleron.domain.note.Noted
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HandleNotedUpdateUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var handleNotedUpdateUseCase: HandleNotedUpdateUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setup() {
        dm = TestDependencyManager()
        repository = dm.getRepository()
        handleNotedUpdateUseCase = dm.getUseCase()
    }

    // Simple fake Noted model for test isolation
    private data class TestNoted(
        override val noteId: Int? = null,
        override val note: Note? = null
    ): Noted

    @Test
    fun invoke_NoteIsNull_ReturnsNullAndNoActions() = runTest {
        val noted = TestNoted(note = null)

        val result = handleNotedUpdateUseCase(noted)

        assertNull(result)
    }

    @Test
    fun invoke_NoteTextIsBlank_RemovesNoteAndReturnsNull() = runTest {
        repository.add(Note(0, "Note 1"))
        val noteId = repository.add(Note(0, "Note 2"))
        val note = repository.get(noteId)!!.copy(noteText = "")
        val countBefore = repository.getAll().count()

        val noted = TestNoted(noteId = noteId, note = note)

        val result = handleNotedUpdateUseCase(noted)

        assertNull(result)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore.dec(), countAfter)
    }

    @Test
    fun invoke_ExistingNoteIdMatchesAndNonZero_UpdatesNoteAndReturnsSameId() = runTest {
        val noteId = repository.add(Note(0, "Note 1"))
        val updatedNoteText = "Updated note"
        val note = repository.get(noteId)!!.copy(noteText = updatedNoteText)
        val noted = TestNoted(noteId = noteId, note = note)

        val result = handleNotedUpdateUseCase(noted)

        assertEquals(noteId, result)

        val updatedNote = repository.get(result!!)
        assertEquals(note, updatedNote)
    }

    @Test
    fun invoke_NoteIdIsZero_AddsNoteAndReturnsNewId() = runTest {
        val noteText = "Add new note"
        val note = Note(id = 0, noteText = noteText)
        val noted = TestNoted(noteId = null, note = note)

        val result = handleNotedUpdateUseCase(noted)

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
        val noted = TestNoted(noteId = noteId2, note = note)

        val result = handleNotedUpdateUseCase(noted)

        assertNotEquals(noteId1, result)
        assertNotEquals(noteId2, result)

        val addedNote = repository.getAll().firstOrNull { it.noteText == noteText }
        assertEquals(result, addedNote?.id)
    }
}
