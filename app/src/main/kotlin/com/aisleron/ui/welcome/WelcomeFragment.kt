package com.aisleron.ui.welcome

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.aisleron.R
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerImpl
import com.aisleron.ui.settings.WelcomePreferences
import com.aisleron.ui.settings.WelcomePreferencesImpl
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WelcomeFragment(
    fabHandler: FabHandler? = null,
    private val welcomePreferences: WelcomePreferences? = null,
    private val addEditFragmentListener: AddEditFragmentListener? = null,

    ) : Fragment() {

    private val _fabHandler = fabHandler
    private lateinit var _welcomePreferences: WelcomePreferences
    private lateinit var _addEditFragmentListener: AddEditFragmentListener


    companion object {
        fun newInstance() = WelcomeFragment()
    }

    private val viewModel: WelcomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    private fun initializeFab() {
        val fabHandler = _fabHandler ?: FabHandlerImpl(this.requireActivity())
        fabHandler.setFabItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initializeFab()

        _welcomePreferences = welcomePreferences ?: WelcomePreferencesImpl(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        _addEditFragmentListener =
            addEditFragmentListener ?: (this.requireActivity() as AddEditFragmentListener)

        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        val txtWelcomeLoadSampleItems: TextView = view.findViewById(R.id.txt_welcome_load_sample_items)
        txtWelcomeLoadSampleItems.text =
            Html.fromHtml(getString(R.string.welcome_load_sample_items), FROM_HTML_MODE_LEGACY)

        txtWelcomeLoadSampleItems.setOnClickListener { _ ->
            viewModel.createSampleData()
        }

        val txtWelcomeAddOwnProduct: TextView = view.findViewById(R.id.txt_welcome_add_own_product)
        txtWelcomeAddOwnProduct.text =
            Html.fromHtml(getString(R.string.welcome_add_own_product), FROM_HTML_MODE_LEGACY)

        txtWelcomeAddOwnProduct.setOnClickListener { _ ->
            _welcomePreferences.setInitialised()
            _addEditFragmentListener.addEditActionCompleted()
        }

        val txtWelcomeImportDb: TextView = view.findViewById(R.id.txt_welcome_import_db)
        txtWelcomeImportDb.text =
            Html.fromHtml(getString(R.string.welcome_import_db), FROM_HTML_MODE_LEGACY)

        txtWelcomeImportDb.setOnClickListener { _ ->
            _welcomePreferences.setInitialised()
            val navController = this.findNavController()
            navController.popBackStack(R.id.nav_welcome, true)
            navController.navigate(R.id.nav_settings)
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.welcomeUiState.collect {
                    when (it) {
                        is WelcomeViewModel.WelcomeUiState.Error -> {
                            displayErrorSnackBar(it.errorCode, it.errorMessage)
                        }

                        is WelcomeViewModel.WelcomeUiState.SampleDataLoaded -> {
                            _welcomePreferences.setInitialised()
                            _addEditFragmentListener.addEditActionCompleted()
                        }

                        else -> Unit
                    }
                }
            }
        }

        return view
    }

    private fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?
    ) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)
        ErrorSnackBar().make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
    }
}