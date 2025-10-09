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

package com.aisleron.domain.product.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows

class UpdateProductUseCaseTest {

    private lateinit var dm: TestDependencyManager
    private lateinit var updateProductUseCase: UpdateProductUseCase
    private lateinit var repository: ProductRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        repository = dm.getRepository<ProductRepository>()
        updateProductUseCase = dm.getUseCase<UpdateProductUseCase>()
    }

    private suspend fun existingProduct(): Product = repository.get(1)!!

    private fun noteRepository(): NoteRepository = dm.getRepository<NoteRepository>()

    @Test
    fun updateProduct_IsDuplicateName_ThrowsException() = runTest {
        val existingProduct = existingProduct()
        val id = repository.add(
            existingProduct.copy(id = 0, name = "Product 2", inStock = !existingProduct.inStock)
        )

        val updateProduct = repository.get(id)!!.copy(name = existingProduct.name)

        assertThrows<AisleronException.DuplicateProductNameException> {
            updateProductUseCase(updateProduct)
        }
    }

    @Test
    fun updateProduct_ProductExists_RecordUpdated() = runTest {
        val existingProduct = existingProduct()
        val updateProduct = existingProduct.copy(
            name = existingProduct.name + " Updated",
            inStock = !existingProduct.inStock
        )

        val countBefore: Int = repository.getAll().count()

        updateProductUseCase(updateProduct)

        val updatedProduct: Product? = repository.getByName(updateProduct.name)
        assertNotNull(updatedProduct)
        assertEquals(updateProduct.id, updatedProduct?.id)
        assertEquals(updateProduct.name, updatedProduct?.name)
        assertEquals(updateProduct.inStock, updatedProduct?.inStock)

        val countAfter: Int = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun updateProduct_ProductDoesNotExist_RecordCreated() = runTest {
        val existingProduct = existingProduct()
        val newProduct = existingProduct.copy(
            id = 1030535,
            name = existingProduct.name + " Inserted"
        )

        val countBefore: Int = repository.getAll().count()

        updateProductUseCase(newProduct)

        val updatedProduct: Product? = repository.getByName(newProduct.name)
        assertNotNull(updatedProduct)
        assertEquals(newProduct.name, updatedProduct?.name)
        assertEquals(newProduct.inStock, updatedProduct?.inStock)

        val countAfter: Int = repository.getAll().count()
        assertEquals(countBefore + 1, countAfter)
    }

    private suspend fun getProductWithNote(): Product {
        val noteText = "Note for product update"
        val noteId = noteRepository().add(Note(0, noteText))
        val note = noteRepository().get(noteId)

        val productWithNote = existingProduct().copy(noteId = noteId, note = note)
        repository.update(productWithNote)

        return productWithNote
    }

    @Test
    fun updateProduct_ProductNoteChanged_NoteUpdated() = runTest {
        val product = getProductWithNote()
        val note = product.note!!.copy(noteText = "Updated note text")
        val updateProduct = product.copy(note = note)

        updateProductUseCase(updateProduct)

        val updatedNote = noteRepository().get(note.id)
        assertEquals(note, updatedNote)
    }

    @Test
    fun updateProduct_ProductNoteAdded_NoteAdded() = runTest {
        val note = getNewNote()
        val updateProduct = existingProduct().copy(note = note)

        updateProductUseCase(updateProduct)

        val updatedProduct = repository.get(updateProduct.id)
        assertNotNull(updatedProduct?.noteId)

        val addedNote = noteRepository().get(updatedProduct?.noteId ?: 0)
        assertEquals(note.noteText, addedNote?.noteText)
    }

    @Test
    fun updateProduct_ProductNoteCleared_NoteDeleted() = runTest {
        val product = getProductWithNote()
        val note = product.note!!.copy(noteText = "")
        val updateProduct = product.copy(note = note)

        updateProductUseCase(updateProduct)

        val updatedProduct = repository.get(product.id)
        assertNull(updatedProduct?.noteId)

        val updatedNote = noteRepository().get(note.id)
        assertNull(updatedNote)
    }

    @Test
    fun updateProduct_ProductNoteIdIsZero_NoteAdded() = runTest {
        val note = getNewNote()
        val updateProduct = existingProduct().copy(note = note, noteId = 0)

        updateProductUseCase(updateProduct)

        val updatedProduct = repository.get(updateProduct.id)
        assertNotEquals(0, updatedProduct?.noteId)

        val addedNote = noteRepository().get(updatedProduct?.noteId ?: 0)
        assertEquals(note.noteText, addedNote?.noteText)
    }

    private fun getNewNote(): Note = Note(
        id = 0,
        noteText = "Add note to existing product"
    )
}