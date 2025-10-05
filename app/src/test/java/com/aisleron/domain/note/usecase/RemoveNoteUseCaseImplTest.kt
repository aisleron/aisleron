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

import com.aisleron.data.TestDataManager
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class RemoveNoteUseCaseImplTest {
    private lateinit var testData: TestDataManager
    private lateinit var useCase: RemoveNoteUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        repository = testData.getRepository<NoteRepository>()
        useCase = RemoveNoteUseCaseImpl(testData.getRepository<NoteRepository>())
    }

    @Test
    fun invoke_ExistingNoteProvided_NoteRemoved() = runTest {
        val note = "Existing Note 1"
        val existingItem = Note(
            id = repository.add(Note(id = 0, note = note)),
            note = note
        )

        repository.add(Note(id = 0, note = "Existing Note 2"))
        val countBefore = repository.getAll().count()

        useCase(existingItem)

        val countAfter = repository.getAll().count()
        val deletedItem = repository.get(existingItem.id)

        assertEquals(countBefore - 1, countAfter)
        assertNull(deletedItem)
    }

    @Test
    fun invoke_NonExistingNoteProvided_NothingRemoved() = runTest {
        val note = "Existing Note 1"
        val existingItem = Note(id = 9000, note = note)
        repository.add(Note(id = 0, note = note))
        repository.add(Note(id = 0, note = "Existing Note 2"))
        val countBefore = repository.getAll().count()

        useCase(existingItem)

        val countAfter = repository.getAll().count()

        assertEquals(countBefore, countAfter)
    }
}