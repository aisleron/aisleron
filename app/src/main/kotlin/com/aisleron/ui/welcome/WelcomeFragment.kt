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

package com.aisleron.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.aisleron.R
import com.aisleron.databinding.FragmentWelcomeBinding
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.AisleronFragment
import com.aisleron.ui.FabHandler
import com.aisleron.ui.settings.WelcomePreferences
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class WelcomeFragment(
    private val fabHandler: FabHandler,
    private val welcomePreferences: WelcomePreferences
) : Fragment(), AisleronFragment {

    companion object {
        fun newInstance(
            fabHandler: FabHandler,
            welcomePreferences: WelcomePreferences
        ) = WelcomeFragment(
            fabHandler, welcomePreferences
        )
    }

    private val viewModel: WelcomeViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        viewModel.checkForProducts()
    }

    private fun initializeFab() {
        fabHandler.setFabItems(this.requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initializeFab()

        val binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        setWindowInsetListeners(this, binding.root, false, null)

        with(binding.txtWelcomeLoadSampleItems) {
            text =
                Html.fromHtml(getString(R.string.welcome_load_sample_items), FROM_HTML_MODE_LEGACY)

            setOnClickListener { _ ->
                viewModel.createSampleData()
            }
        }

        with(binding.txtWelcomeAddOwnProduct) {
            text = Html.fromHtml(getString(R.string.welcome_add_own_product), FROM_HTML_MODE_LEGACY)
            setOnClickListener { _ ->
                welcomePreferences.setInitialised(requireContext())
                navigateTo(R.id.nav_in_stock)
            }
        }

        with(binding.txtWelcomeImportDb) {
            text = Html.fromHtml(getString(R.string.welcome_import_db), FROM_HTML_MODE_LEGACY)
            setOnClickListener { _ ->
                welcomePreferences.setInitialised(requireContext())
                navigateTo(R.id.nav_settings)
            }
        }

        with(binding.txtWelcomeDocumentation) {
            text = Html.fromHtml(getString(R.string.welcome_documentation), FROM_HTML_MODE_LEGACY)
            setOnClickListener { _ ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW, getString(R.string.aisleron_documentation_url).toUri()
                )
                startActivity(browserIntent)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.welcomeUiState.collect {
                        when (it) {
                            is WelcomeViewModel.WelcomeUiState.Error -> {
                                displayErrorSnackBar(it.errorCode, it.errorMessage)
                            }

                            is WelcomeViewModel.WelcomeUiState.SampleDataLoaded -> {
                                welcomePreferences.setInitialised(requireContext())
                                navigateTo(R.id.nav_in_stock)
                            }

                            else -> Unit
                        }

                        viewModel.clearState()
                    }
                }

                launch {
                    viewModel.productsLoaded.collect { loaded ->
                        binding.txtWelcomeLoadSampleItems.isEnabled = !loaded
                    }
                }
            }
        }

        return binding.root
    }

    private fun navigateTo(@IdRes resId: Int) {
        val navController = findNavController()
        navController.navigate(resId, null)
    }

    private fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?
    ) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)
        ErrorSnackBar().make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
    }
}