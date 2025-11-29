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

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.FragmentNoteBinding
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.bundles.Bundler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class NoteDialogFragment() : DialogFragment() {
    private val viewModel: NoteDialogViewModel by viewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentNoteBinding.inflate(layoutInflater)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.loading)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, null) // we’ll override click later
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()

        alertDialog.setOnShowListener {
            val okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                viewModel.saveNote()
            }
        }

        binding.edtNotes.doAfterTextChanged {
            viewModel.updateNote(it.toString())
            binding.layoutNotes.error = null
        }

        val noteDialogBundle = Bundler().getNoteDialogBundle(arguments)
        lifecycleScope.launch {
            viewModel.hydrate(noteDialogBundle.noteParentRef)

            alertDialog.setTitle(getString(R.string.note_dialog_title, viewModel.parentName))
            binding.edtNotes.setText(viewModel.noteText)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        state != NoteDialogViewModel.UiState.Loading

                    when (state) {
                        is NoteDialogViewModel.UiState.Error -> {
                            binding.layoutNotes.error = getString(
                                AisleronExceptionMap().getErrorResourceId(state.errorCode),
                                state.errorMessage
                            )
                        }

                        is NoteDialogViewModel.UiState.Success -> {
                            val imm =
                                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                                        as android.view.inputmethod.InputMethodManager

                            imm.hideSoftInputFromWindow(binding.edtNotes.windowToken, 0)
                            dismiss()
                        }

                        else -> {}
                    }
                }
            }
        }

        return alertDialog
    }

    companion object {
        fun newInstance(noteParentRef: NoteParentRef) =
            NoteDialogFragment().apply {
                arguments = Bundler().makeNotesDialogBundle(noteParentRef)
            }
    }
}