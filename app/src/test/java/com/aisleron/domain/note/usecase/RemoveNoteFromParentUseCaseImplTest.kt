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
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class RemoveNoteFromParentUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var removeNoteFromParentUseCase: RemoveNoteFromParentUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setup() {
        dm = TestDependencyManager()
        repository = dm.getRepository()
        removeNoteFromParentUseCase = dm.getUseCase()
    }

    private suspend fun getProductWithNote(): Product {
        val noteText = "Note for product update"
        val noteId = repository.add(Note(0, noteText))
        val note = repository.get(noteId)

        val productRepository = dm.getRepository<ProductRepository>()
        val productWithNote = productRepository.getAll().first().copy(noteId = noteId, note = note)
        productRepository.update(productWithNote)

        return productWithNote
    }

    private suspend fun getLocationWithNote(): Location {
        val noteText = "Note for location update"
        val noteId = repository.add(Note(0, noteText))
        val note = repository.get(noteId)

        val locationRepo = dm.getRepository<LocationRepository>()
        val locationWithNote = locationRepo.getAll().first().copy(noteId = noteId, note = note)
        locationRepo.update(locationWithNote)

        return locationWithNote
    }

    @Test
    fun invoke_ParentIsProduct_NoteRemovedFromProduct() = runTest {
        val product = getProductWithNote()
        val noteId = product.noteId!!

        removeNoteFromParentUseCase(product, noteId)

        val updatedParent = dm.getRepository<ProductRepository>().get(product.id)!!
        assertNull(updatedParent.noteId)
    }

    @Test
    fun invoke_ParentNoteIdMismatch_NoteNotRemovedFromParent() = runTest {
        val parent = getProductWithNote()

        removeNoteFromParentUseCase(parent, -1)

        val updatedParent = dm.getRepository<ProductRepository>().get(parent.id)!!
        assertNotNull(updatedParent.noteId)
    }

    @Test
    fun invoke_ParentNoteIdIsNull_ParentNoteIdRemainsNull() = runTest {
        val parent = dm.getRepository<ProductRepository>().getAll().first()

        removeNoteFromParentUseCase(parent, -1)

        val updatedParent = dm.getRepository<ProductRepository>().get(parent.id)!!
        assertNull(updatedParent.noteId)
    }

    @Test
    fun invoke_ParentIsLocation_NoteRemovedFromProduct() = runTest {
        val location = getLocationWithNote()
        val noteId = location.noteId!!

        removeNoteFromParentUseCase(location, noteId)

        val updatedParent = dm.getRepository<LocationRepository>().get(location.id)!!
        assertNull(updatedParent.noteId)
    }

    @Test
    fun invoke_LocationNoteIdIsNull_LocationNoteIdRemainsNull() = runTest {
        val parent = dm.getRepository<LocationRepository>().getAll().first()

        removeNoteFromParentUseCase(parent, -1)

        val updatedParent = dm.getRepository<LocationRepository>().get(parent.id)!!
        assertNull(updatedParent.noteId)
    }


}