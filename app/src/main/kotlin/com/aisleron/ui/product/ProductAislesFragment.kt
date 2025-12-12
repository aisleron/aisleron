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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentProductAislesBinding
import com.aisleron.databinding.ListItemProductAisleBinding
import com.aisleron.ui.AisleronFragment
import com.aisleron.ui.aisle.AislePickerDialogFragment
import com.aisleron.ui.bundles.AislePickerBundle
import kotlinx.coroutines.launch

class ProductAislesFragment : Fragment(), AisleronFragment {

    private var _binding: FragmentProductAislesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by lazy {
        ViewModelProvider(requireParentFragment())[ProductViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.setFragmentResultListener(
            AISLE_PICKER_REQUEST_KEY,
            this
        ) { _, bundle ->
            val selectedAisleId = bundle.getInt(AislePickerDialogFragment.KEY_SELECTED_AISLE_ID, -1)
            val addNewAisle = bundle.getBoolean(AislePickerDialogFragment.KEY_ADD_NEW_AISLE, false)

            if (selectedAisleId != -1) {
                viewModel.updateProductAisle(selectedAisleId)
            }
            if (addNewAisle) {
                // TODO: Handle add new aisle
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductAislesBinding.inflate(inflater, container, false)
        setWindowInsetListeners(this, binding.root, true, null, applyMargins = false, applyBottomPadding = true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productAislesAdapter = ProductAislesAdapter { productAisleInfo ->
            viewModel.requestLocationAisles(productAisleInfo)
        }
        binding.productAislesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAislesAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.productAisles.collect {
                        productAislesAdapter.submitList(it)
                    }
                }

                launch {
                    viewModel.aislesForLocation.collect { data ->
                        if (data != null) {
                            showAislePickerDialog(data)
                            viewModel.onAislesDialogShown()
                        }
                    }
                }
            }
        }
    }

    private fun showAislePickerDialog(data: AislePickerBundle) {
        AislePickerDialogFragment.newInstance(data, AISLE_PICKER_REQUEST_KEY)
            .show(childFragmentManager, AislePickerDialogFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

    companion object {
        private const val AISLE_PICKER_REQUEST_KEY = "aislePickerRequest"
    }

    private class ProductAislesAdapter(private val onItemClicked: (ProductAisleInfo) -> Unit) :
        ListAdapter<ProductAisleInfo, ProductAislesAdapter.ViewHolder>(DiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ListItemProductAisleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position), onItemClicked)
        }

        class ViewHolder(private val binding: ListItemProductAisleBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(
                productAisleInfo: ProductAisleInfo, onItemClicked: (ProductAisleInfo) -> Unit
            ) {
                binding.locationName.text = productAisleInfo.locationName
                binding.aisleName.text = productAisleInfo.aisleName
                itemView.setOnClickListener {
                    onItemClicked(productAisleInfo)
                }
            }
        }

        companion object {
            private val DiffCallback = object : DiffUtil.ItemCallback<ProductAisleInfo>() {
                override fun areItemsTheSame(
                    oldItem: ProductAisleInfo, newItem: ProductAisleInfo
                ): Boolean {
                    return oldItem.locationId == newItem.locationId && oldItem.aisleId == newItem.aisleId
                }

                override fun areContentsTheSame(
                    oldItem: ProductAisleInfo, newItem: ProductAisleInfo
                ): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }
}
