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
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.ui.note.NoteParentType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class GetNoteParentUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getNoteParentUseCase: GetNoteParentUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setup() {
        dm = TestDependencyManager()
        repository = dm.getRepository()
        getNoteParentUseCase = dm.getUseCase()
    }

    private suspend fun getProduct(): Product {
        return dm.getRepository<ProductRepository>().getAll().first()
    }

    @Test
    fun invoke_ParentIsProduct_ReturnProduct() = runTest {
        val productId = getProduct().id

        val parent = getNoteParentUseCase(NoteParentType.PRODUCT, productId)

        assertNotNull(parent)
        assertTrue(parent is Product)
    }

    @Test
    fun invoke_InvalidParentId_ReturnNull() = runTest {
        val parent = getNoteParentUseCase(NoteParentType.PRODUCT, -1)

        assertNull(parent)
    }
}