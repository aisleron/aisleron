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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.FragmentProductBinding
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.ApplicationTitleUpdateListenerImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerImpl
import com.aisleron.ui.bundles.AddEditProductBundle
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductFragment(
    private val addEditFragmentListener: AddEditFragmentListener? = null,
    private val applicationTitleUpdateListener: ApplicationTitleUpdateListener,
    private val fabHandler: FabHandler
) : Fragment(), MenuProvider {

    private val productViewModel: ProductViewModel by viewModel()
    private var _binding: FragmentProductBinding? = null
    private lateinit var _addEditFragmentListener: AddEditFragmentListener

    private val binding get() = _binding!!

    private var appTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addEditProductBundle = Bundler().getAddEditProductBundle(arguments)

        appTitle = when (addEditProductBundle.actionType) {
            AddEditProductBundle.ProductAction.ADD -> getString(R.string.add_product)
            AddEditProductBundle.ProductAction.EDIT -> getString(R.string.edit_product)
        }

        productViewModel.hydrate(
            addEditProductBundle.productId,
            addEditProductBundle.inStock ?: false
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fabHandler.setFabItems(this.requireActivity())

        _addEditFragmentListener =
            addEditFragmentListener ?: (this.requireActivity() as AddEditFragmentListener)

        _binding = FragmentProductBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.productUiState.collect {
                    when (it) {
                        ProductViewModel.ProductUiState.Success -> {
                            _addEditFragmentListener.addEditActionCompleted()
                        }

                        ProductViewModel.ProductUiState.Empty -> Unit
                        ProductViewModel.ProductUiState.Loading -> Unit
                        is ProductViewModel.ProductUiState.Error -> {
                            displayErrorSnackBar(it.errorCode, it.errorMessage)
                        }

                        is ProductViewModel.ProductUiState.Updated -> {
                            binding.chkProductInStock.isChecked = productViewModel.inStock
                            binding.edtProductName.setText(productViewModel.productName)
                        }
                    }
                }
            }
        }

        val chk = binding.chkProductInStock
        chk.setOnClickListener { chk.isChecked = !chk.isChecked }
        return binding.root
    }

    private fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?
    ) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)
        ErrorSnackBar().make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
    }

    private fun saveProduct(productName: String, inStock: Boolean) {
        if (productName.isBlank()) return
        productViewModel.saveProduct(productName, inStock)
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
        edtProductName.postDelayed({
            edtProductName.requestFocus()
            val imm =
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(edtProductName, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            name: String?,
            inStock: Boolean,
            addEditFragmentListener: AddEditFragmentListener
        ) =
            ProductFragment(
                addEditFragmentListener, ApplicationTitleUpdateListenerImpl(), FabHandlerImpl()
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
                saveProduct(
                    binding.edtProductName.text.toString(),
                    binding.chkProductInStock.isChecked
                )
                true
            }

            else -> false
        }
    }
}