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

package com.aisleron.ui.aisle

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.shoppinglist.AisleShoppingListItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class AisleDialogImpl(private val aisleViewModel: AisleViewModel) : AisleDialog {
    private var dialog: AlertDialog? = null
    private lateinit var txtAisleName: TextInputEditText
    private var addMoreAisles = false

    override fun observeLifecycle(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                aisleViewModel.uiState.collect { state ->
                    when (state) {
                        is AisleViewModel.AisleUiState.Error -> showError(state.errorCode)
                        is AisleViewModel.AisleUiState.Success -> closeDialog()
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun showAddDialog(context: Context, locationId: Int) {
        val builder = getDialogBuilder(context, R.string.add_aisle)
            .setNegativeButton(R.string.add_another, null)

        dialog = builder.create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    addAisle(txtAisleName.text.toString(), locationId, false)
                }

                getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                    addAisle(txtAisleName.text.toString(), locationId, true)
                }
            }
        }

        showDialog()
    }

    override fun showEditDialog(context: Context, aisle: AisleShoppingListItem) {
        val builder = getDialogBuilder(context, R.string.edit_aisle)
        txtAisleName.setText(aisle.name)
        dialog = builder.create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    addMoreAisles = false
                    aisleViewModel.updateAisleName(aisle, txtAisleName.text.toString())
                }
            }
        }

        showDialog()
    }

    private fun showDialog() {
        txtAisleName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }

        txtAisleName.requestFocus()
        dialog?.show()
    }

    private fun getDialogBuilder(context: Context, titleResourceId: Int): AlertDialog.Builder {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_aisle, null)
        txtAisleName = dialogView.findViewById(R.id.edt_aisle_name)
        txtAisleName.doAfterTextChanged {
            dialog?.findViewById<TextInputLayout>(R.id.edt_aisle_name_layout)?.error = ""
        }

        return AlertDialog.Builder(context)
            .setView(dialogView)
            .setNeutralButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.done, null)
            .setTitle(titleResourceId)
    }

    private fun addAisle(aisleName: String, locationId: Int, allowMoreAisles: Boolean) {
        addMoreAisles = allowMoreAisles
        aisleViewModel.addAisle(aisleName, locationId)
    }

    private fun showError(code: AisleronException.ExceptionCode) {
        val layout = dialog?.findViewById<TextInputLayout>(R.id.edt_aisle_name_layout)
        layout?.error = AisleronExceptionMap().getErrorResourceId(code).let { id ->
            layout?.context?.getString(id)
        }

        aisleViewModel.clearState()
    }

    private fun closeDialog() {
        if (dialog == null) return
        if (!addMoreAisles) {
            dialog?.dismiss()
            dialog = null
        } else {
            dialog?.findViewById<TextInputEditText>(R.id.edt_aisle_name)?.setText("")
        }

        aisleViewModel.clearState()
    }
}
