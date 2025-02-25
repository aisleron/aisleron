package com.aisleron.ui.welcome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.aisleron.R
import com.aisleron.databinding.FragmentWelcomeBinding
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.FabHandler
import com.aisleron.ui.settings.WelcomePreferences
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class WelcomeFragment(
    private val fabHandler: FabHandler,
    private val welcomePreferences: WelcomePreferences,
    private val addEditFragmentListener: AddEditFragmentListener
) : Fragment() {

    companion object {
        fun newInstance(
            fabHandler: FabHandler,
            welcomePreferences: WelcomePreferences,
            addEditFragmentListener: AddEditFragmentListener
        ) = WelcomeFragment(
            fabHandler, welcomePreferences, addEditFragmentListener
        )
    }

    private val viewModel: WelcomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
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
                addEditFragmentListener.addEditActionCompleted(requireActivity())
            }
        }

        with(binding.txtWelcomeImportDb) {
            text = Html.fromHtml(getString(R.string.welcome_import_db), FROM_HTML_MODE_LEGACY)
            setOnClickListener { _ ->
                welcomePreferences.setInitialised(requireContext())
                val navController = this@WelcomeFragment.findNavController()
                navController.popBackStack(R.id.nav_welcome, true)
                navController.navigate(R.id.nav_settings)
            }
        }

        with(binding.txtWelcomeDocumentation) {
            text = Html.fromHtml(getString(R.string.welcome_documentation), FROM_HTML_MODE_LEGACY)
            setOnClickListener { _ ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(getString(R.string.aisleron_documentation_url))
                )
                startActivity(browserIntent)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.welcomeUiState.collect {
                    when (it) {
                        is WelcomeViewModel.WelcomeUiState.Error -> {
                            displayErrorSnackBar(it.errorCode, it.errorMessage)
                        }

                        is WelcomeViewModel.WelcomeUiState.SampleDataLoaded -> {
                            welcomePreferences.setInitialised(requireContext())
                            addEditFragmentListener.addEditActionCompleted(requireActivity())
                        }

                        else -> Unit
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
}