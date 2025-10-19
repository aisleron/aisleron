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

package com.aisleron.ui.note

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteParent
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCase
import com.aisleron.domain.note.usecase.GetNoteParentUseCase
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteDialogViewModelTest : KoinTest {
    private lateinit var viewModel: NoteDialogViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        viewModel = get<NoteDialogViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private suspend fun getNoteParentWithoutNote(): NoteParent {
        return get<ProductRepository>().getAll().first()
    }

    private suspend fun getNoteParentWithNote(): NoteParent {
        val noteRepository = get<NoteRepository>()
        val noteId = noteRepository.add(Note(0, "Note Dialog Test Note"))
        val note = noteRepository.get(noteId)!!

        val productRepository = get<ProductRepository>()
        val product = productRepository.getAll().first()
        productRepository.update(product.copy(noteId = noteId))

        return product.copy(noteId = noteId, note = note)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_CopyEntityViewModelReturned() {
        val vm = NoteDialogViewModel(
            get<GetNoteParentUseCase>(),
            get<ApplyNoteChangesUseCase>()
        )

        Assert.assertNotNull(vm)
    }

    @Test
    fun hydrate_FirstRun_SetViewModelValues() = runTest {
        val parent = getNoteParentWithNote()

        viewModel.hydrate(NoteParentType.PRODUCT, parent.id)

        assertEquals(parent.name, viewModel.parentName)
        assertEquals(parent.note!!.noteText, viewModel.noteText)
    }

    @Test
    fun hydrate_SecondRun_DoNotOverwriteNote() = runTest {
        val parent = getNoteParentWithNote()
        val newNoteText = "New Note text to keep after hydrate"
        viewModel.hydrate(NoteParentType.PRODUCT, parent.id)
        viewModel.updateNote(newNoteText)

        viewModel.hydrate(NoteParentType.PRODUCT, parent.id)

        assertEquals(newNoteText, viewModel.noteText)
    }

    @Test
    fun hydrate_InvalidParentProvided_LoadDefaultValues() = runTest {
        viewModel.hydrate(NoteParentType.PRODUCT, -1)

        assertEquals("", viewModel.parentName)
        assertEquals("", viewModel.noteText)
    }

    @Test
    fun updateNote_NoteTextProvided_ViewMoelNoteTextUpdated() = runTest {
        val parent = getNoteParentWithoutNote()
        val newNoteText = "New Note text"
        viewModel.hydrate(NoteParentType.PRODUCT, parent.id)

        viewModel.updateNote(newNoteText)

        assertEquals(newNoteText, viewModel.noteText)
    }

    @Test
    fun saveNote_NewNote_NoteSaved() = runTest {
        val parent = getNoteParentWithoutNote()
        val newNoteText = "New Note text"
        viewModel.hydrate(NoteParentType.PRODUCT, parent.id)
        viewModel.updateNote(newNoteText)

        viewModel.saveNote()

        assertEquals(NoteDialogViewModel.UiState.Success, viewModel.uiState.value)

        val updatedParent = get<ProductRepository>().get(parent.id)
        assertNotNull(updatedParent?.noteId)

        val note = get<NoteRepository>().get(updatedParent.noteId)
        assertEquals(newNoteText, note?.noteText)
    }

    @Test
    fun saveNote_UpdatedNote_NoteSaved() = runTest {
        val parent = getNoteParentWithNote()
        val newNoteText = "New Note text"
        viewModel.hydrate(NoteParentType.PRODUCT, parent.id)
        viewModel.updateNote(newNoteText)

        viewModel.saveNote()

        assertEquals(NoteDialogViewModel.UiState.Success, viewModel.uiState.value)

        val note = get<NoteRepository>().get(parent.noteId!!)
        assertEquals(newNoteText, note?.noteText)
    }

    @Test
    fun saveNote_ParentIsNull_NothingAdded() = runTest {
        val newNoteText = "New Note text"
        val noteRepository = get<NoteRepository>()
        val countBefore = noteRepository.getAll().count()
        viewModel.hydrate(NoteParentType.PRODUCT, -1)
        viewModel.updateNote(newNoteText)

        viewModel.saveNote()

        val note = noteRepository.getAll().firstOrNull { it.noteText == newNoteText }
        assertNull(note)

        val countAfter = noteRepository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun saveNote_ExceptionRaised_UiStateISError() = runTest {
        val exceptionMessage = "Error on Save Note"

        declare<ApplyNoteChangesUseCase> {
            object : ApplyNoteChangesUseCase {
                override suspend fun invoke(item: NoteParent, note: Note?): Int? {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<NoteDialogViewModel>()
        val parent = getNoteParentWithNote()
        vm.hydrate(NoteParentType.PRODUCT, parent.id)

        vm.saveNote()

        val uiState = vm.uiState.value
        assertTrue { uiState is NoteDialogViewModel.UiState.Error }
        with(uiState as NoteDialogViewModel.UiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }
}