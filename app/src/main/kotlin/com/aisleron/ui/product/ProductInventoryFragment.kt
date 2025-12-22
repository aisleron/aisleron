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

package com.aisleron.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.FragmentProductInventoryBinding
import com.aisleron.domain.product.TrackingMode
import com.aisleron.ui.AisleronFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class ProductInventoryFragment(
    viewModel: ProductInventoryViewModel? = null
) : Fragment(), AisleronFragment {
    private var _binding: FragmentProductInventoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductInventoryViewModel by lazy {
        viewModel ?: ViewModelProvider(requireParentFragment())[ProductViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductInventoryBinding.inflate(inflater, container, false)
        setWindowInsetListeners(
            this, binding.root, false, null, applyMargins = false, applyBottomPadding = true
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the display names and values from resources
        val trackingModeDisplayNames = resources.getStringArray(R.array.tracking_method_names)
            .toMutableList()
            .apply { add(getString(R.string.tracking_default)) }

        val trackingModeValues = resources.getStringArray(R.array.tracking_method_values)
            .toMutableList()
            .apply { add(TrackingMode.DEFAULT.value) }

        // Create a list of pairs (displayName, enum)
        val trackingModes = trackingModeDisplayNames.mapIndexed { index, displayName ->
            val value = trackingModeValues.getOrNull(index)
            val mode = TrackingMode.fromValue(value)
            displayName to mode
        }

        binding.edtTrackingMode.keyListener = null
        binding.edtTrackingMode.setOnClickListener {
            val currentMode = viewModel.uiData.value.trackingMode
            val checkedItemIndex = trackingModes.indexOfFirst { it.second == currentMode }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.tracking_mode))
                .setSingleChoiceItems(
                    trackingModeDisplayNames.toTypedArray(),
                    checkedItemIndex
                ) { dialog, position ->
                    val (displayName, selectedMode) = trackingModes[position]
                    binding.edtTrackingMode.setText(displayName)
                    viewModel.updateTrackingMode(selectedMode)
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        binding.edtUom.doAfterTextChanged {
            val newUom = it?.toString() ?: ""
            viewModel.updateUnitOfMeasure(newUom)
        }

        binding.edtIncrement.doAfterTextChanged {
            val newIncrement = it?.toString()?.toDoubleOrNull() ?: 0.0
            viewModel.updateQtyIncrement(newIncrement)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiData.collect { data ->
                    // Update Increment only if not focused to avoid cursor jumping
                    if (!binding.edtIncrement.isFocused) {
                        val formatted = formatQty(data.qtyIncrement) // Using your helper
                        if (binding.edtIncrement.text.toString() != formatted) {
                            binding.edtIncrement.setText(formatted)
                        }
                    }

                    // Update UOM
                    if (!binding.edtUom.isFocused) {
                        if (binding.edtUom.text.toString() != data.unitOfMeasure) {
                            binding.edtUom.setText(data.unitOfMeasure)
                        }
                    }

                    // Update Tracking mode dropdown
                    val displayedTrackingMode = binding.edtTrackingMode.text.toString()
                    val expectedTrackingMode =
                        trackingModes.find { it.second == data.trackingMode }?.first ?: ""

                    if (displayedTrackingMode != expectedTrackingMode) {
                        binding.edtTrackingMode.setText(expectedTrackingMode)
                    }
                }
            }
        }
    }

    private fun formatQty(qty: Double): String =
        DecimalFormat("0.###").format(qty)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

}