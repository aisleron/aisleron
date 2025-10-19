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

import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.aisleron.AppCompatActivityTestImpl
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteParent
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCase
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NoteDialogFragmentTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @JvmField
    @Rule
    val activityScenarioRule = ActivityScenarioRule(AppCompatActivityTestImpl::class.java)

    @Before
    fun setUp() {
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private fun newInstanceTest(noteParentId: Int, noteParentType: NoteParentType) {
        val dialog = NoteDialogFragment.newInstance(noteParentId, noteParentType)
        assertNotNull(dialog)
    }

    @Test
    fun newInstance_IsCalledForProduct_ReturnNoteDialog() {
        newInstanceTest(1, NoteParentType.PRODUCT)
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
    fun show_ValidParentWithNote_DisplayTitleAndNote() = runTest {
        var title = ""
        val noteParent = getNoteParentWithNote()
        val dialog = NoteDialogFragment.newInstance(noteParent.id, NoteParentType.PRODUCT)

        activityScenarioRule.scenario.onActivity {
            title = it.getString(R.string.note_dialog_title, noteParent.name)
            dialog.show(it.supportFragmentManager, "noteDialog")
        }

        onView(withText(title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(withText(noteParent.note?.noteText)))
    }

    @Test
    fun show_ValidParentWithoutNote_DisplayBlankNote() = runTest {
        var title = ""
        val noteParent = getNoteParentWithoutNote()
        val dialog = NoteDialogFragment.newInstance(noteParent.id, NoteParentType.PRODUCT)

        activityScenarioRule.scenario.onActivity {
            title = it.getString(R.string.note_dialog_title, noteParent.name)
            dialog.show(it.supportFragmentManager, "noteDialog")
        }

        onView(withText(title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(withText("")))
    }

    @Test
    fun cancel_IsCalled_NoteNotChanged() = runTest {
        val noteParent = getNoteParentWithNote()
        val initialNoteText = noteParent.note!!.noteText
        val dialog = NoteDialogFragment.newInstance(noteParent.id, NoteParentType.PRODUCT)

        activityScenarioRule.scenario.onActivity {
            dialog.show(it.supportFragmentManager, "noteDialog")
        }

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(ViewActions.typeText(" Added text to note"))

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        val updatedNote = get<NoteRepository>().get(noteParent.noteId!!)!!
        assertEquals(initialNoteText, updatedNote.noteText)
    }

    @Test
    fun ok_HasError_ShowError() = runTest {
        var resultErrorMessage = ""
        val exceptionMessage = "Apply Note Changes Error"
        declare<ApplyNoteChangesUseCase> {
            object : ApplyNoteChangesUseCase {
                override suspend fun invoke(item: NoteParent, note: Note?): Int? {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val noteParent = getNoteParentWithNote()
        val dialog = NoteDialogFragment.newInstance(noteParent.id, NoteParentType.PRODUCT)
        activityScenarioRule.scenario.onActivity {
            dialog.show(it.supportFragmentManager, "noteDialog")
            resultErrorMessage = it.getString(R.string.generic_error, exceptionMessage)
        }

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .perform(click())

        onView(withText(resultErrorMessage))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun ok_IsValidNote_ApplyNoteChanges() = runTest {
        val noteParent = getNoteParentWithoutNote()
        val noteText = "Add note to parent test"
        val dialog = NoteDialogFragment.newInstance(noteParent.id, NoteParentType.PRODUCT)
        activityScenarioRule.scenario.onActivity {
            dialog.show(it.supportFragmentManager, "noteDialog")
        }

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(ViewActions.typeText(noteText))

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .perform(click())

        val product = get<ProductRepository>().get(noteParent.id)
        assertNotNull(product?.noteId)

        val note = get<NoteRepository>().get(product.noteId)
        assertEquals(noteText, note?.noteText)

    }
}