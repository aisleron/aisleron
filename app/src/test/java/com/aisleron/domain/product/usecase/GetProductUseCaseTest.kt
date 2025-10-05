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

import com.aisleron.data.TestDataManager
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.note.usecase.GetNoteUseCaseImpl
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetProductUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var getProductUseCase: GetProductUseCase
    private lateinit var repository: ProductRepository

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        repository = testData.getRepository<ProductRepository>()
        getProductUseCase = GetProductUseCaseImpl(
            repository,
            GetNoteUseCaseImpl(testData.getRepository<NoteRepository>())
        )
    }

    @Test
    fun getProduct_NonExistentId_ReturnNull() = runTest {
        Assertions.assertNull( getProductUseCase(2001))
    }

    @Test
    fun getProduct_ExistingId_ReturnProduct() = runTest {
        val product = getProductUseCase(1)
        Assertions.assertNotNull(product)
        Assertions.assertEquals(1, product!!.id)
    }

    @Test
    fun getProduct_ProductHasNoNote_NoteIsNull() = runTest {
        val productId = repository.getAll().first { it.noteId == null }.id
        val product = getProductUseCase(productId)
        Assertions.assertNotNull(product)
        Assertions.assertNull(product?.noteId)
        Assertions.assertNull(product?.note)
    }

    @Test
    fun getProduct_ProductHasNote_NoteReturned() = runTest {
        val product = repository.getAll().first { it.noteId == null }
        val noteText = "Test note returns for product"
        val note = Note(
            id = testData.getRepository<NoteRepository>().add(Note(0, noteText)),
            noteText = noteText
        )

        repository.update(product.copy(noteId = note.id))
        val productResult = getProductUseCase(product.id)

        Assertions.assertEquals(note.id, productResult?.noteId)
        Assertions.assertEquals(note, productResult?.note)
    }
}