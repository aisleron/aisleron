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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
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
import com.aisleron.ui.settings.ProductPreferences
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductFragment(
    private val addEditFragmentListener: AddEditFragmentListener,
    private val applicationTitleUpdateListener: ApplicationTitleUpdateListener,
    private val productPreferences: ProductPreferences,
    private val fabHandler: FabHandler,
) : Fragment(), MenuProvider, AisleronFragment {

    private var showAddShopFab: Boolean = false
    private val productViewModel: ProductViewModel by viewModel()
    private var _binding: FragmentProductBinding? = null

    private val binding get() = _binding!!

    private var appTitle: String = ""

    private var tabsAdapter: ProductTabsAdapter? = null

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            showSaveConfirmationDialog(requireContext())
        }
    }

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
                addEditProductBundle.aisleId
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setWindowInsetListeners(this, binding.root, false, R.dimen.text_margin,
            applyMargins = true,
            applyBottomPadding = false
        )

        initialiseTabs()
        showHideExtraOptions()

        binding.txtToggleExtraOptions.setOnClickListener {
            val visible = binding.layoutExtraOptions.isVisible
            productPreferences.setShowExtraOptions(!visible)
            showHideExtraOptions()
        }

        binding.edtProductName.doAfterTextChanged {
            val newText = it?.toString() ?: ""
            productViewModel.updateProductName(newText)
        }

        binding.chkProductInStock.setOnClickListener {
            val chk = binding.chkProductInStock
            chk.isChecked = !chk.isChecked
            productViewModel.updateInStock(chk.isChecked)
        }

        val menuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectUiData() }
                launch { collectUiState() }
                launch { collectDirtyFlag() }
            }
        }
    }

    private suspend fun collectDirtyFlag() {
        productViewModel.isDirty.collect { isDirty ->
            backCallback.isEnabled = isDirty
        }
    }

    private suspend fun collectUiState() {
        productViewModel.productUiState.collect {
            when (it) {
                ProductViewModel.ProductUiState.Success -> {
                    productViewModel.clearState()
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

    private suspend fun collectUiData() {
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

    private fun showHideExtraOptions() {
        val toggle = binding.txtToggleExtraOptions
        val extraOptions = binding.layoutExtraOptions
        var expandDrawable: Int
        if (productPreferences.showExtraOptions()) {
            extraOptions.visibility = View.VISIBLE
            expandDrawable = R.drawable.baseline_expand_down_24
        } else {
            extraOptions.visibility = View.GONE
            expandDrawable = R.drawable.baseline_expand_right_24
        }

        toggle.setCompoundDrawablesRelativeWithIntrinsicBounds(expandDrawable, 0, 0, 0)
        setShowAddShopFab()

        binding.pgrProductOptions.post {
            val lastSelectedTab = productPreferences.getLastSelectedTab()
            binding.pgrProductOptions.setCurrentItem(lastSelectedTab, false)
        }
    }

    private fun initialiseTabs() {
        // Setup tabs & pager
        tabsAdapter = ProductTabsAdapter(this)
        val viewPager = binding.pgrProductOptions
        viewPager.adapter = tabsAdapter
        val lastSelectedTab = productPreferences.getLastSelectedTab()
        viewPager.setCurrentItem(lastSelectedTab, false)

        TabLayoutMediator(binding.tabProductOptions, viewPager) { tab, position ->
            tab.text = when (position) {
                ProductTabsAdapter.ProductTab.TAB_NOTES.ordinal -> getString(R.string.tab_notes)
                ProductTabsAdapter.ProductTab.TAB_AISLES.ordinal -> getString(R.string.product_tab_aisles)
                ProductTabsAdapter.ProductTab.TAB_INVENTORY.ordinal -> getString(R.string.product_tab_inventory)
                //ProductTabsAdapter.ProductTab.TAB_BARCODES.ordinal -> getString(R.string.product_tab_barcodes)
                else -> ""
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                productPreferences.setLastSelectedTab(position)
                setShowAddShopFab()
            }
        })
    }

    private fun setShowAddShopFab() {
        showAddShopFab = productPreferences.showExtraOptions()
                && binding.pgrProductOptions.currentItem == ProductTabsAdapter.ProductTab.TAB_AISLES.ordinal

        if (showAddShopFab) {
            fabHandler.setFabItems(requireActivity(), FabHandler.FabOption.ADD_SHOP)
        } else {
            fabHandler.setFabItems(requireActivity())
        }
    }

    private fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?
    ) {
        val anchorView = if (showAddShopFab) fabHandler.getFabView(requireActivity()) else null

        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)

        ErrorSnackBar().make(
            requireView(), snackBarMessage, Snackbar.LENGTH_SHORT, anchorView
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        applicationTitleUpdateListener.applicationTitleUpdated(requireActivity(), appTitle)

        // Don't show the keyboard by default if extra options are visible.
        if (productPreferences.showExtraOptions()) return

        val edtProductName = binding.edtProductName
        edtProductName.doOnLayout {
            edtProductName.requestFocus()
            val imm =
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(edtProductName, InputMethodManager.SHOW_IMPLICIT)
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

            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> false
        }
    }

    private fun showSaveConfirmationDialog(context: Context) {
        val builder = MaterialAlertDialogBuilder(context)
        builder
            .setTitle(getString(R.string.save_changes_title))
            .setMessage(R.string.save_changes_message)
            .setNegativeButton(R.string.discard) { _, _ ->
                backCallback.isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setPositiveButton(R.string.save) { _, _ ->
                productViewModel.saveProduct()
            }
            .setNeutralButton(R.string.keep_editing, null)

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }
}
