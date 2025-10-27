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

package com.aisleron.ui.shop

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
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.FragmentShopBinding
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.AisleronFragment
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.FabHandler
import com.aisleron.ui.bundles.AddEditLocationBundle
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.loyaltycard.LoyaltyCardProvider
import com.aisleron.ui.settings.ShopPreferences
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ShopFragment(
    private val addEditFragmentListener: AddEditFragmentListener,
    private val applicationTitleUpdateListener: ApplicationTitleUpdateListener,
    private val fabHandler: FabHandler,
    private val loyaltyCardProvider: LoyaltyCardProvider,
    private val shopPreferences: ShopPreferences
) : Fragment(), MenuProvider, AisleronFragment {
    private val shopViewModel: ShopViewModel by viewModel()
    private var _binding: FragmentShopBinding? = null

    private val binding get() = _binding!!

    private var appTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loyaltyCardProvider.registerLauncher(this) { loyaltyCard ->
            shopViewModel.setLoyaltyCard(loyaltyCard)
        }

        val addEditLocationBundle = Bundler().getAddEditLocationBundle(arguments)
        appTitle = when (addEditLocationBundle.actionType) {
            AddEditLocationBundle.LocationAction.ADD -> getString(R.string.add_location)
            AddEditLocationBundle.LocationAction.EDIT -> getString(R.string.edit_location)
        }

        if (savedInstanceState == null) {
            shopViewModel.hydrate(addEditLocationBundle.locationId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fabHandler.setFabItems(this.requireActivity())

        _binding = FragmentShopBinding.inflate(inflater, container, false)
        setWindowInsetListeners(this, binding.root, false, R.dimen.text_margin)

        binding.btnLookupLoyaltyCard.setOnClickListener {
            lookupLoyaltyCard()
        }

        binding.btnDeleteLoyaltyCard.setOnClickListener {
            shopViewModel.removeLoyaltyCard()
        }

        initialiseTabs()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    shopViewModel.uiData.collect { data ->
                        // Update EditText only if needed to avoid cursor jumping
                        if (binding.edtShopName.text.toString() != data.locationName) {
                            binding.edtShopName.setText(data.locationName)
                        }

                        if (binding.swcShopPinned.isChecked != data.pinned) {
                            binding.swcShopPinned.isChecked = data.pinned
                        }

                        if (binding.swcShopShowUnmappedProducts.isChecked != data.showDefaultAisle) {
                            binding.swcShopShowUnmappedProducts.isChecked = data.showDefaultAisle
                        }

                        if (binding.edtShopLoyaltyCard.text.toString() != data.loyaltyCardName) {
                            binding.edtShopLoyaltyCard.setText(data.loyaltyCardName)
                        }
                    }
                }

                launch {
                    shopViewModel.shopUiState.collect {
                        when (it) {
                            ShopViewModel.ShopUiState.Success -> {
                                addEditFragmentListener.addEditActionCompleted(requireActivity())
                            }

                            is ShopViewModel.ShopUiState.Error -> {
                                displayErrorSnackBar(it.errorCode, it.errorMessage)
                            }

                            ShopViewModel.ShopUiState.Empty,
                            ShopViewModel.ShopUiState.Loading -> Unit
                        }
                    }
                }
            }
        }

        binding.edtShopName.doAfterTextChanged {
            val newText = it?.toString() ?: ""
            shopViewModel.updateLocationName(newText)
        }

        binding.swcShopPinned.setOnClickListener {
            val swc = binding.swcShopPinned
            shopViewModel.updatePinned(swc.isChecked)
        }

        binding.swcShopShowUnmappedProducts.setOnClickListener {
            val swc = binding.swcShopShowUnmappedProducts
            shopViewModel.updateShowDefaultAisle(swc.isChecked)
        }

        return binding.root
    }

    private fun showHideExtraOptions() {
        val toggle = binding.txtToggleExtraOptions
        val extraOptions = binding.layoutExtraOptions
        var expandDrawable: Int
        if (shopPreferences.showExtraOptions(requireContext())) {
            extraOptions.visibility = View.VISIBLE
            expandDrawable = R.drawable.baseline_expand_down_24
        } else {
            extraOptions.visibility = View.GONE
            expandDrawable = R.drawable.baseline_expand_right_24
        }

        toggle.setCompoundDrawablesRelativeWithIntrinsicBounds(expandDrawable, 0, 0, 0)

    }

    private fun initialiseTabs() {
        showHideExtraOptions()

        // Expand / collapse extra options
        binding.txtToggleExtraOptions.setOnClickListener {
            val visible = binding.layoutExtraOptions.isVisible
            shopPreferences.setShowExtraOptions(requireContext(), !visible)
            showHideExtraOptions()
        }

        // Setup tabs & pager
        val tabsAdapter = ShopTabsAdapter(this)
        val viewPager = binding.pgrShopOptions
        viewPager.adapter = tabsAdapter


        TabLayoutMediator(binding.tabShopOptions, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_notes)
                else -> ""
            }
        }.attach()
    }

    private fun lookupLoyaltyCard() {
        try {
            loyaltyCardProvider.lookupLoyaltyCardShortcut(requireContext())
        } catch (_: AisleronException.LoyaltyCardProviderException) {
            loyaltyCardProvider.getNotInstalledDialog(requireContext()).show()
        } catch (e: Exception) {
            displayErrorSnackBar(AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message)
        }
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

        val edtLocationName = binding.edtShopName
        edtLocationName.doOnLayout {
            edtLocationName.requestFocus()
            val imm =
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(edtLocationName, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(
            name: String?,
            addEditFragmentListener: AddEditFragmentListener,
            applicationTitleUpdateListener: ApplicationTitleUpdateListener,
            fabHandler: FabHandler,
            loyaltyCardProvider: LoyaltyCardProvider,
            shopPreferences: ShopPreferences
        ) =
            ShopFragment(
                addEditFragmentListener,
                applicationTitleUpdateListener,
                fabHandler,
                loyaltyCardProvider,
                shopPreferences
            ).apply {
                arguments = Bundler().makeAddLocationBundle(name)
            }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_edit_fragment_main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        //NOTE: If you override onMenuItemSelected, OnSupportNavigateUp will only be called when returning false
        return when (menuItem.itemId) {
            R.id.mnu_btn_save -> {
                shopViewModel.saveLocation()
                true
            }

            else -> false
        }
    }
}