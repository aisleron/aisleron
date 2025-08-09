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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.FragmentProductBinding
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.AisleronFragment
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.FabHandler
import com.aisleron.ui.bundles.AddEditProductBundle
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductFragment(
    private val addEditFragmentListener: AddEditFragmentListener,
    private val applicationTitleUpdateListener: ApplicationTitleUpdateListener,
    private val fabHandler: FabHandler
) : Fragment(), MenuProvider, AisleronFragment {

    private val productViewModel: ProductViewModel by viewModel()
    private var _binding: FragmentProductBinding? = null

    private val binding get() = _binding!!

    private var appTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addEditProductBundle = Bundler().getAddEditProductBundle(arguments)

        appTitle = when (addEditProductBundle.actionType) {
            AddEditProductBundle.ProductAction.ADD -> getString(R.string.add_product)
            AddEditProductBundle.ProductAction.EDIT -> getString(R.string.edit_product)
        }

        if (savedInstanceState == null) {
            productViewModel.hydrate(
                addEditProductBundle.productId,
                addEditProductBundle.inStock ?: false,
                addEditProductBundle.locationId,
                addEditProductBundle.aisleId
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fabHandler.setFabItems(this.requireActivity())

        _binding = FragmentProductBinding.inflate(inflater, container, false)
        setWindowInsetListeners(this, binding.root, false, R.dimen.text_margin)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    productViewModel.uiData.collect { data ->
                        // Update EditText only if needed to avoid cursor jumping
                        if (binding.edtProductName.text.toString() != data.productName) {
                            binding.edtProductName.setText(data.productName)
                        }

                        // Update CheckedTextView
                        if (binding.chkProductInStock.isChecked != data.inStock) {
                            binding.chkProductInStock.isChecked = data.inStock
                        }
                    }
                }

                launch {
                    productViewModel.productUiState.collect {
                        when (it) {
                            ProductViewModel.ProductUiState.Success -> {
                                addEditFragmentListener.addEditActionCompleted(requireActivity())
                            }

                            is ProductViewModel.ProductUiState.Error -> {
                                displayErrorSnackBar(it.errorCode, it.errorMessage)
                            }

                            ProductViewModel.ProductUiState.Loading,
                            ProductViewModel.ProductUiState.Empty -> Unit
                        }
                    }
                }
            }
        }

        binding.edtProductName.doAfterTextChanged {
            val newText = it?.toString() ?: ""
            if (productViewModel.uiData.value.productName != newText) {
                productViewModel.updateProductName(newText)
            }
        }

        binding.chkProductInStock.setOnClickListener {
            val chk = binding.chkProductInStock
            chk.isChecked = !chk.isChecked
            if (productViewModel.uiData.value.inStock != chk.isChecked) {
                productViewModel.updateInStock(chk.isChecked)
            }
        }

        return binding.root
    }

    private fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?
    ) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)
        ErrorSnackBar().make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        applicationTitleUpdateListener.applicationTitleUpdated(requireActivity(), appTitle)

        val edtProductName = binding.edtProductName
        edtProductName.doOnLayout {
            edtProductName.requestFocus()
            val imm =
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(edtProductName, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            name: String?,
            inStock: Boolean,
            addEditFragmentListener: AddEditFragmentListener,
            applicationTitleUpdateListener: ApplicationTitleUpdateListener,
            fabHandler: FabHandler
        ) =
            ProductFragment(
                addEditFragmentListener, applicationTitleUpdateListener, fabHandler
            ).apply {
                arguments = Bundler().makeAddProductBundle(name, inStock)
            }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_edit_fragment_main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.mnu_btn_save -> {
                productViewModel.saveProduct()
                true
            }

            else -> false
        }
    }
}