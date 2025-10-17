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
import org.junit.jupiter.api.assertNull

class AddNoteToParentUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var addNoteToParentUseCase: AddNoteToParentUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setup() {
        dm = TestDependencyManager()
        repository = dm.getRepository()
        addNoteToParentUseCase = dm.getUseCase()
    }

    private suspend fun getNote(noteText: String): Note {
        val noteId = repository.add(Note(0, noteText))
        return repository.get(noteId)!!
    }

    private suspend fun getProduct(): Product {
        return dm.getRepository<ProductRepository>().getAll().first()
    }

    private suspend fun getProductWithNote(): Product {
        val note = getNote("Note for product")

        val productWithNote = getProduct().copy(noteId = note.id, note = note)
        dm.getRepository<ProductRepository>().update(productWithNote)

        return productWithNote
    }

    @Test
    fun invoke_ParentIsProduct_ProductUpdatedWithNoteId() = runTest {
        val note = getNote("Test add to product")
        val product = getProduct()

        addNoteToParentUseCase(product,note.id)

        val updatedProduct = dm.getRepository<ProductRepository>().get(product.id)
        assertEquals(note.id, updatedProduct?.noteId)
    }

    @Test
    fun invoke_ParentHasExistingNote_ExistingNoteDeleted() = runTest {
        val product = getProductWithNote()
        val note = getNote("Replacement Note")

        addNoteToParentUseCase(product,note.id)

        val updatedProduct = dm.getRepository<ProductRepository>().get(product.id)
        assertEquals(note.id, updatedProduct?.noteId)

        val deletedNote = dm.getRepository<NoteRepository>().get(product.noteId!!)
        assertNull(deletedNote)
    }
}