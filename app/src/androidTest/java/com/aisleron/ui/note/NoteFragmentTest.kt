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

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aisleron.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.hamcrest.Matchers.emptyString
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class NoteFragmentTest {
    private lateinit var vm: NoteViewModelTestImpl

    @Before
    fun setUp() {
        vm = NoteViewModelTestImpl()

    }

    private fun getFragmentScenario(): FragmentScenario<NoteFragmentTestImpl> =
        launchFragmentInContainer<NoteFragmentTestImpl>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { NoteFragmentTestImpl(vm) },
            fragmentArgs = null
        )

    @Test
    fun onCreateView_ViewModelHasNoNote_ShowEmptyNote() {
        getFragmentScenario()

        onView(withId(R.id.edt_notes))
            .check(matches(withText(emptyString())))
    }

    @Test
    fun onCreateView_ViewModelHasNote_ShowNote() {
        val noteText = "Show Note"
        vm.emitNote(noteText)

        getFragmentScenario()

        onView(withId(R.id.edt_notes))
            .check(matches(withText(noteText)))
    }

    @Test
    fun onViewModelValuesChanged_NoteIsChanged_ShowNewNote() {
        vm.emitNote("Starting note")
        getFragmentScenario()

        val noteText = "Updated note from view model"
        vm.emitNote(noteText)

        onView(withId(R.id.edt_notes))
            .check(matches(withText(noteText)))
    }

    @Test
    fun onViewModelValuesChanged_NoteIsSame_ShowExistingNote() {
        vm.emitNote("Starting note")
        getFragmentScenario()

        val noteText = vm.noteText
        vm.emitNote(noteText)

        onView(withId(R.id.edt_notes))
            .check(matches(withText(noteText)))
    }

    @Test
    fun doAfterTextChanged_NewNoteEntered_ViewModelReceivesNote() {
        vm.emitNote("Starting note")
        getFragmentScenario()

        val noteText = "Updated note from view model"
        onView(withId(R.id.edt_notes)).perform(clearText())
        onView(withId(R.id.edt_notes)).perform(typeText(noteText))

        assertEquals(noteText, vm.noteText)
    }


    class NoteViewModelTestImpl : NoteViewModel {
        private var _noteText = ""
        val noteText: String get() = _noteText

        private val _noteFlow = MutableStateFlow("")
        override val noteFlow: StateFlow<String> = _noteFlow

        override fun updateNote(noteText: String) {
            _noteText = noteText
        }

        fun emitNote(note: String) {
            _noteFlow.value = note
        }
    }

    class NoteFragmentTestImpl(
        override val viewModel: NoteViewModelTestImpl
    ) : NoteFragment<NoteViewModelTestImpl>()
}