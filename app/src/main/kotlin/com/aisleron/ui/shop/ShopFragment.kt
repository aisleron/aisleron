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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.FragmentShopBinding
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.FabHandler
import com.aisleron.ui.bundles.AddEditLocationBundle
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.loyaltycard.LoyaltyCardProvider
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ShopFragment(
    private val addEditFragmentListener: AddEditFragmentListener,
    private val applicationTitleUpdateListener: ApplicationTitleUpdateListener,
    private val fabHandler: FabHandler,
    private val loyaltyCardProvider: LoyaltyCardProvider
) : Fragment(), MenuProvider {
    private val shopViewModel: ShopViewModel by viewModel()
    private var _binding: FragmentShopBinding? = null

    private val binding get() = _binding!!

    private var appTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loyaltyCardProvider.registerLauncher(this) { loyaltyCard ->
            shopViewModel.addLoyaltyCard(loyaltyCard)
            loyaltyCard?.let {
                Toast.makeText(
                    requireContext(),
                    "Loyalty Card Selected: ${it.id}, ${it.name}",
                    Toast.LENGTH_LONG
                ).show()

                loyaltyCardProvider.displayLoyaltyCard(it.providerCardId)
            } ?: run {
                Toast.makeText(requireContext(), "No Loyalty Card ID returned", Toast.LENGTH_SHORT).show()
            }
        }

        val addEditLocationBundle = Bundler().getAddEditLocationBundle(arguments)
        appTitle = when (addEditLocationBundle.actionType) {
            AddEditLocationBundle.LocationAction.ADD -> getString(R.string.add_location)
            AddEditLocationBundle.LocationAction.EDIT -> getString(R.string.edit_location)
        }

        shopViewModel.hydrate(addEditLocationBundle.locationId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fabHandler.setFabItems(this.requireActivity())

        _binding = FragmentShopBinding.inflate(inflater, container, false)

        binding.btnGetLoyaltyCard.setOnClickListener {
            loyaltyCardProvider.lookupLoyaltyCardShortcut()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                shopViewModel.shopUiState.collect {
                    when (it) {
                        ShopViewModel.ShopUiState.Success -> {
                            addEditFragmentListener.addEditActionCompleted(requireActivity())
                        }

                        ShopViewModel.ShopUiState.Empty -> Unit
                        ShopViewModel.ShopUiState.Loading -> Unit
                        is ShopViewModel.ShopUiState.Error -> {
                            displayErrorSnackBar(it.errorCode, it.errorMessage)
                        }

                        is ShopViewModel.ShopUiState.Updated -> {
                            binding.edtShopName.setText(shopViewModel.locationName)
                            binding.swcShopPinned.isChecked = shopViewModel.pinned
                            binding.swcShopShowUnmappedProducts.isChecked =
                                shopViewModel.showDefaultAisle

                        }
                    }
                }
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

    private fun saveShop(shopName: String, pinned: Boolean, showDefaultAisle: Boolean) {
        if (shopName.isBlank()) return
        shopViewModel.saveLocation(shopName, pinned, showDefaultAisle)
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
        edtLocationName.postDelayed({
            edtLocationName.requestFocus()
            val imm =
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(edtLocationName, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    companion object {

        @JvmStatic
        fun newInstance(
            name: String?,
            addEditFragmentListener: AddEditFragmentListener,
            applicationTitleUpdateListener: ApplicationTitleUpdateListener,
            fabHandler: FabHandler,
            loyaltyCardProvider: LoyaltyCardProvider
        ) =
            ShopFragment(
                addEditFragmentListener,
                applicationTitleUpdateListener,
                fabHandler,
                loyaltyCardProvider
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
                saveShop(
                    binding.edtShopName.text.toString(),
                    binding.swcShopPinned.isChecked,
                    binding.swcShopShowUnmappedProducts.isChecked
                )
                true
            }

            else -> false
        }
    }
}