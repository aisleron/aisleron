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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetNotesUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getNotesUseCase: GetNotesUseCase
    private lateinit var repository: NoteRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        repository = dm.getRepository<NoteRepository>()
        getNotesUseCase = dm.getUseCase()
    }

    @Test
    fun invoke_NonExistentNoteIdsProvided_ReturnEmptyList() = runTest {
        val resultItem = getNotesUseCase(listOf(-1, -2, -3)).first()
        assertEquals(0, resultItem.size)
    }

    @Test
    fun invoke_ExistingNoteIdsProvided_ReturnNote() = runTest {
        val note = "Existing Note"
        val existingItem1 = repository.add(Note(id = 0, noteText = note))
        val existingItem2 = repository.add(Note(id = 0, noteText = note))
        val itemList = listOf(existingItem1, existingItem2)

        val resultItems = getNotesUseCase(itemList).first()

        val resultIds = resultItems.map { it.id }

        assertEquals(itemList.sorted(), resultIds.sorted())
    }

}