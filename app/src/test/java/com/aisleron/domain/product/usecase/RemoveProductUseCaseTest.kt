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
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.note.usecase.RemoveNoteUseCaseImpl
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveProductUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var removeProductUseCase: RemoveProductUseCase
    private lateinit var existingProduct: Product
    private lateinit var repository: ProductRepository

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        repository = testData.getRepository<ProductRepository>()

        removeProductUseCase = RemoveProductUseCaseImpl(
            repository,
            RemoveNoteUseCaseImpl(testData.getRepository<NoteRepository>())
        )

        existingProduct = runBlocking { repository.get(1)!! }
    }

    @Test
    fun removeProduct_IsExistingProduct_ProductRemoved() = runTest {
        val countBefore: Int = repository.getAll().count()

        removeProductUseCase(existingProduct.id)
        val removedProduct: Product? = repository.get(existingProduct.id)
        val countAfter: Int = repository.getAll().count()

        assertNull(removedProduct)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeProduct_IsNonExistentProduct_NoProductsRemoved() = runTest {
        val countBefore: Int = repository.getAll().count()

        removeProductUseCase(repository.getAll().maxOf { it.id } + 1000)
        val countAfter: Int = repository.getAll().count()

        assertEquals(countBefore, countAfter)
    }

    @Test
    fun removeProduct_ProductRemoved_AisleProductsRemoved() = runTest {
        val aisleProductRepository = testData.getRepository<AisleProductRepository>()
        val aisleProductList = aisleProductRepository.getAll()
            .filter { it.product.id == existingProduct.id }

        val aisleProductCountProduct = aisleProductList.count()
        val aisleProductCountBefore = aisleProductRepository.getAll().count()

        removeProductUseCase(existingProduct.id)
        val aisleProductCountAfter = aisleProductRepository.getAll().count()

        assertEquals(
            aisleProductCountBefore - aisleProductCountProduct,
            aisleProductCountAfter
        )
    }

    @Test
    fun removeProduct_PassProductObject_ProductRemoved() = runTest {
        val countBefore: Int = repository.getAll().count()

        removeProductUseCase(existingProduct)
        val removedProduct: Product? = repository.get(existingProduct.id)
        val countAfter: Int = repository.getAll().count()

        assertNull(removedProduct)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeProduct_ProductHasNote_NoteRemoved() = runTest {
        val noteRepository = testData.getRepository<NoteRepository>()
        val product = repository.getAll().first { it.noteId == null }
        val noteText = "Test note deletes for product"
        val noteId = noteRepository.add(Note(0, noteText))
        repository.update(product.copy(noteId = noteId))

        removeProductUseCase(product.id)
        val noteAfter = noteRepository.get(noteId)

        assertNull(noteAfter)
    }
}