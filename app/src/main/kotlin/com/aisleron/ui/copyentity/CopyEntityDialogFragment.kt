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

package com.aisleron.ui.copyentity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.DialogCopyEntityBinding
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CopyEntityDialogFragment : DialogFragment() {

    private val viewModel: CopyEntityViewModel by viewModel()
    var onCopySuccess: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogCopyEntityBinding.inflate(layoutInflater)

        val copyEntityBundle = Bundler().getCopyEntityBundle(arguments)

        // Set UI values
        binding.edtEntityNameLayout.hint = copyEntityBundle.nameHint
        binding.edtEntityName.setText(copyEntityBundle.defaultName)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(copyEntityBundle.title)
            .setPositiveButton(android.R.string.ok, null) // We'll handle click manually
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val newName = binding.edtEntityName.text.toString().trim()
                if (newName.isEmpty()) {
                    binding.edtEntityNameLayout.error = getString(R.string.entity_name_required)
                } else {
                    binding.edtEntityNameLayout.error = null
                    viewModel.copyEntity(copyEntityBundle.type, newName)
                }
            }
        }

        binding.edtEntityName.post {
            binding.edtEntityName.requestFocus()
            binding.edtEntityName.selectAll()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        binding.edtEntityName.doAfterTextChanged {
            binding.edtEntityNameLayout.error = null
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        state != CopyEntityViewModel.CopyUiState.Loading
                    when (state) {
                        is CopyEntityViewModel.CopyUiState.Error -> {
                            binding.edtEntityNameLayout.error = getString(
                                AisleronExceptionMap().getErrorResourceId(state.errorCode),
                                state.errorMessage
                            )
                        }

                        is CopyEntityViewModel.CopyUiState.Success -> {
                            val imm =
                                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                                        as android.view.inputmethod.InputMethodManager

                            imm.hideSoftInputFromWindow(binding.edtEntityName.windowToken, 0)

                            onCopySuccess?.invoke()
                            dismiss()
                        }

                        else -> {}
                    }
                }
            }
        }

        return dialog
    }

    companion object {
        fun newInstance(
            type: CopyEntityType, title: String, defaultName: String, nameHint: String
        ) =
            CopyEntityDialogFragment().apply {
                arguments = Bundler().makeCopyEntityBundle(type, title, defaultName, nameHint)
            }
    }
}
