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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

class RemoveNoteUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var removeNoteUseCase: RemoveNoteUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        repository = dm.getRepository<NoteRepository>()
        removeNoteUseCase = dm.getUseCase()
    }

    private suspend fun getProductWithNote(): Product {
        val noteText = "Note to test Remove"
        val noteId = repository.add(Note(0, noteText))
        val note = repository.get(noteId)

        val productRepository = dm.getRepository<ProductRepository>()
        val productWithNote = productRepository.getAll().first().copy(noteId = noteId, note = note)
        productRepository.update(productWithNote)

        return productWithNote
    }

    @Test
    fun invoke_ExistingNoteProvided_NoteRemoved() = runTest {
        val parent = getProductWithNote()
        val existingItem = parent.note!!
        repository.add(Note(id = 0, noteText = "Existing Note 2"))
        val countBefore = repository.getAll().count()

        removeNoteUseCase(parent, existingItem)

        val deletedItem = repository.get(existingItem.id)
        assertNull(deletedItem)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun invoke_NonExistingNoteProvided_NothingRemoved() = runTest {
        val parent = getProductWithNote()
        val note = "Existing Note 1"
        val existingItem = Note(id = 9000, noteText = note)
        repository.add(Note(id = 0, noteText = note))
        repository.add(Note(id = 0, noteText = "Existing Note 2"))
        val countBefore = repository.getAll().count()

        removeNoteUseCase(parent, existingItem)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun invoke_DeleteById_NoteRemoved() = runTest {
        val parent = getProductWithNote()
        val noteId = parent.noteId!!
        repository.add(Note(id = 0, noteText = "Existing Note 2"))
        val countBefore = repository.getAll().count()

        removeNoteUseCase(parent, noteId)

        val noteAfter = repository.get(noteId)
        assertNull(noteAfter)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun invoke_InvalidNoteIdProvided_NothingRemoved() = runTest {
        val parent = getProductWithNote()
        repository.add(Note(id = 0, noteText = "Existing Note 2"))
        val countBefore = repository.getAll().count()

        removeNoteUseCase(parent, -1)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun invoke_ParentNoteIdMismatch_NoteNotRemoved() = runTest {
        val parent = getProductWithNote()
        val noteId = repository.add(Note(id = 0, noteText = "Existing Note 2"))
        val countBefore = repository.getAll().count()

        removeNoteUseCase(parent, noteId)

        val noteAfter = repository.get(noteId)
        assertNotNull(noteAfter)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun invoke_ParentNoteIdIsNull_NoteNotRemoved() = runTest {
        val parent = dm.getRepository<ProductRepository>().getAll().first()
        val noteId = repository.add(Note(id = 0, noteText = "Existing Note 2"))
        val countBefore = repository.getAll().count()

        removeNoteUseCase(parent, noteId)

        val noteAfter = repository.get(noteId)
        assertNotNull(noteAfter)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun invoke_ParentIdMatches_RemoveNoteIdFromParent() = runTest {
        val parent = getProductWithNote()

        removeNoteUseCase(parent, parent.noteId!!)

        val parentAfter = dm.getRepository<ProductRepository>().get(parent.id)!!
        assertNull(parentAfter.noteId)
    }
}