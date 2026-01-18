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

package com.aisleron.ui.aisle

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.DialogAisleBinding
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.bundles.AisleDialogBundle
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.copyentity.CopyEntityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AisleDialogFragment : DialogFragment() {
    enum class AisleDialogAction {
        ADD_SINGLE, ADD_MULTIPLE, EDIT
    }

    private val viewModel: AisleViewModel by viewModel()
    private var _binding: DialogAisleBinding? = null
    private val binding get() = _binding!!
    private var addMoreAisles = false
    private var positiveButtonClickListener: ((view: View) -> Unit)? = null
    private var neutralButtonClickListener: ((view: View) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAisleBinding.inflate(layoutInflater)
        val requestKey = requireArguments().getString(ARG_REQUEST_KEY, "")
        val aisleBundle = Bundler().getAisleDialogBundle(arguments)
        viewModel.hydrate(aisleBundle.aisleId, aisleBundle.locationId)

        val builder = when (aisleBundle.action) {
            AisleDialogAction.ADD_SINGLE -> getAddSingleDialogBuilder()
            AisleDialogAction.ADD_MULTIPLE -> getAddMultipleDialogBuilder()
            AisleDialogAction.EDIT -> getEditDialogBuilder()
        }

        val dialog = builder
            .setNegativeButton(android.R.string.cancel, null)
            .setView(binding.root)
            .create()

        dialog.setOnShowListener {
            // Need to override listeners here to make sure the the dialog doesn't close until we want it to
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener(positiveButtonClickListener)

            val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton?.setOnClickListener(neutralButtonClickListener)
        }

        binding.edtAisleName.post {
            binding.edtAisleName.requestFocus()
            binding.edtAisleName.selectAll()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        binding.edtAisleName.doAfterTextChanged {
            binding.edtAisleNameLayout.error = null
            viewModel.setAisleName(binding.edtAisleName.text.toString())
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                            state != CopyEntityViewModel.CopyUiState.Loading
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).isEnabled =
                            state != CopyEntityViewModel.CopyUiState.Loading
                        when (state) {
                            is AisleViewModel.AisleUiState.Error -> showError(state.errorCode)
                            is AisleViewModel.AisleUiState.Success -> closeDialog(
                                state.aisleId, requestKey
                            )

                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.aisleName.collect { name ->
                        if (binding.edtAisleName.text.toString() != name) {
                            binding.edtAisleName.setText(name)
                        }
                    }
                }
            }
        }

        return dialog
    }

    private fun showError(code: AisleronException.ExceptionCode) {
        val layout = binding.edtAisleNameLayout
        layout.error = getString(AisleronExceptionMap().getErrorResourceId(code))
        viewModel.clearState()
    }

    private fun closeDialog(aisleId: Int, requestKey: String) {
        if (!addMoreAisles) {
            setFragmentResult(requestKey, bundleOf(KEY_AISLE_ID to aisleId))
            dismiss()
        } else {
            binding.edtAisleName.setText("")
        }

        viewModel.clearState()
    }

    private fun getAddMultipleDialogBuilder(): MaterialAlertDialogBuilder {
        neutralButtonClickListener = {
            addMoreAisles = true
            viewModel.addAisle()
        }

        return getAddSingleDialogBuilder()
            .setNeutralButton(R.string.add_another, null)
    }

    private fun getAddSingleDialogBuilder(): MaterialAlertDialogBuilder {
        positiveButtonClickListener = {
            addMoreAisles = false
            viewModel.addAisle()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_aisle)
            .setPositiveButton(R.string.done, null)
    }

    private fun getEditDialogBuilder(): MaterialAlertDialogBuilder {
        positiveButtonClickListener = {
            addMoreAisles = false
            viewModel.updateAisleName()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_aisle)
            .setPositiveButton(R.string.done, null)
    }

    companion object {
        const val TAG = "AislePickerDialogFragment"
        const val KEY_AISLE_ID = "aisleId"
        private const val ARG_REQUEST_KEY = "requestKey"

        fun newInstance(
            aisleDialogBundle: AisleDialogBundle,
            requestKey: String
        ): AisleDialogFragment {
            return AisleDialogFragment().apply {
                arguments = Bundler().makeAisleDialogBundle(
                    aisleDialogBundle.aisleId,
                    aisleDialogBundle.action,
                    aisleDialogBundle.locationId
                ).apply {
                    putString(ARG_REQUEST_KEY, requestKey)
                }
            }
        }
    }
}