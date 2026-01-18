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

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.aisleron.R
import com.aisleron.ui.bundles.AislePickerBundle
import com.aisleron.ui.bundles.Bundler
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AislePickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val requestKey = requireArguments().getString(ARG_REQUEST_KEY, "")
        val aislePickerBundle = Bundler().getAislePickerBundle(arguments)
        val aisles = aislePickerBundle.aisles
        val aisleNames = aisles.map { it.name }.toTypedArray()
        val checkedItem = aisles.indexOfFirst { it.id == aislePickerBundle.currentAisleId }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(aislePickerBundle.title)
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.new_aisle) { _, _ ->
                setFragmentResult(requestKey, bundleOf(KEY_ADD_NEW_AISLE to true))
            }
            .setSingleChoiceItems(aisleNames, checkedItem) { dialog, which ->
                val selectedAisleId = aisles[which].id
                setFragmentResult(requestKey, bundleOf(KEY_SELECTED_AISLE_ID to selectedAisleId))
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        const val TAG = "AislePickerDialogFragment"
        const val KEY_SELECTED_AISLE_ID = "selectedAisleId"
        const val KEY_ADD_NEW_AISLE = "addNewAisle"
        private const val ARG_REQUEST_KEY = "requestKey"

        fun newInstance(
            aislePickerBundle: AislePickerBundle,
            requestKey: String
        ): AislePickerDialogFragment {
            return AislePickerDialogFragment().apply {
                arguments = Bundler().makeAislePickerBundle(
                    aislePickerBundle.title,
                    aislePickerBundle.aisles,
                    aislePickerBundle.currentAisleId
                ).apply {
                    putString(ARG_REQUEST_KEY, requestKey)
                }
            }
        }
    }
}
