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
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.ui.note.NoteParentRef
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
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

    private suspend fun getLocation(): Location {
        return dm.getRepository<LocationRepository>().getAll()
            .first { it.type != LocationType.HOME }
    }

    private suspend fun getProduct(): Product {
        return dm.getRepository<ProductRepository>().getAll().first()
    }

    @Test
    fun invoke_ParentIsProduct_ReturnProduct() = runTest {
        val productId = getProduct().id

        val parent = getNoteParentUseCase(NoteParentRef.Product(productId))

        assertNotNull(parent)
        assertTrue(parent is Product)
    }

    @Test
    fun invoke_InvalidProductId_ReturnNull() = runTest {
        val parent = getNoteParentUseCase(NoteParentRef.Product(-1))

        assertNull(parent)
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

    @Test
    fun invoke_ProductHasNote_ReturnParentWithNote() = runTest {
        val product = getProductWithNote()

        val parent = getNoteParentUseCase(NoteParentRef.Product(product.id))!!

        assertNotNull(parent.note)
        assertEquals(product.note, parent.note)
    }

    @Test
    fun invoke_ProductHasNoNote_ReturnParentWithNullNote() = runTest {
        val product = getProduct()

        val parent = getNoteParentUseCase(NoteParentRef.Product(product.id))

        assertNotNull(parent)
        assertNull(parent.note)
    }

    @Test
    fun invoke_ParentIsLocation_ReturnLocation() = runTest {
        val locationId = getLocation().id

        val parent = getNoteParentUseCase(NoteParentRef.Location(locationId))

        assertNotNull(parent)
        assertTrue(parent is Location)
    }

    private suspend fun getLocationWithNote(): Location {
        val noteText = "Note for product update"
        val noteId = repository.add(Note(0, noteText))
        val note = repository.get(noteId)

        val locationRepository = dm.getRepository<LocationRepository>()
        val locationWithNote =
            locationRepository.getAll().first().copy(noteId = noteId, note = note)
        locationRepository.update(locationWithNote)

        return locationWithNote
    }

    @Test
    fun invoke_LocationHasNote_ReturnLocationWithNote() = runTest {
        val location = getLocationWithNote()

        val parent = getNoteParentUseCase(NoteParentRef.Location(location.id))

        assertNotNull(parent?.note)
        assertEquals(location.note, parent.note)
    }

    @Test
    fun invoke_LocationHasNoNote_ReturnParentWithNullNote() = runTest {
        val location = getLocation()

        val parent = getNoteParentUseCase(NoteParentRef.Location(location.id))

        assertNotNull(parent)
        assertNull(parent.note)
    }

    @Test
    fun invoke_InvalidLocationId_ReturnNull() = runTest {
        val parent = getNoteParentUseCase(NoteParentRef.Location(-1))

        assertNull(parent)
    }

    @Test
    fun invoke_ParentHasInvalidNoteId_ReturnNullNote() = runTest {
        val location = getLocation().copy(noteId = -1)
        dm.getRepository<LocationRepository>().update(location)

        val parent = getNoteParentUseCase(NoteParentRef.Location(location.id))

        assertNotNull(parent)
        assertNull(parent.note)
    }
}