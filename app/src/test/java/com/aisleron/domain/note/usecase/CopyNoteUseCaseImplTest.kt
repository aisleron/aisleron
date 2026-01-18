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

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CopyNoteUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var repository: NoteRepository
    private lateinit var copyNoteUseCase: CopyNoteUseCase

    @BeforeEach
    fun setup() {
        dm = TestDependencyManager()
        repository = dm.getRepository()
        copyNoteUseCase = dm.getUseCase()
    }

    private suspend fun getProduct(): Product {
        return dm.getRepository<ProductRepository>().getAll().first()
    }

    @Test
    fun invoke_NoteDoesNotExist_ReturnsNull() = runTest {
        val nonexistentId = 9999

        val result = copyNoteUseCase(getProduct(), nonexistentId)

        assertNull(result, "Expected null when note does not exist")
        assertEquals(0, repository.getAll().count { it.id == nonexistentId })
    }

    @Test
    fun invoke_ExistingNote_CopiesNoteAndReturnsNewId() = runTest {
        val originalText = "Original Note"
        val originalId = repository.add(Note(0, originalText))

        val result = copyNoteUseCase(getProduct(), originalId)

        // Result should be a valid new note ID
        assertNotNull(result)
        assertNotEquals(originalId, result)

        val copiedNote = repository.get(result!!)
        assertNotNull(copiedNote)
        assertEquals(originalText, copiedNote?.noteText)

        // Ensure the repository has both notes now
        val allNotes = repository.getAll()
        assertTrue(allNotes.any { it.id == originalId })
        assertTrue(allNotes.any { it.id == result })
    }

    @Test
    fun invoke_ExistingNote_ModifiesCopyOnly() = runTest {
        val noteId = repository.add(Note(0, "Original Note"))
        val copyId = copyNoteUseCase(getProduct(), noteId)!!

        val copiedNote = repository.get(copyId)!!

        // Update the copied note text
        val updatedCopy = copiedNote.copy(noteText = "Updated Copy")
        repository.update(updatedCopy)

        // Ensure original remains unchanged
        val originalAfterUpdate = repository.get(noteId)!!
        assertEquals("Original Note", originalAfterUpdate.noteText)
        assertEquals("Updated Copy", repository.get(copyId)?.noteText)
    }
}
