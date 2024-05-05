package com.aisleron.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.FragmentProductBinding
import com.aisleron.ui.bundles.AddEditProductBundle
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductFragment : Fragment() {

    private val productViewModel: ProductViewModel by viewModel()
    private var _binding: FragmentProductBinding? = null

    private val binding get() = _binding!!

    private var editMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addEditProductBundle = Bundler().getAddEditProductBundle(arguments)
        editMode = addEditProductBundle.actionType == AddEditProductBundle.ProductAction.EDIT
        productViewModel.hydrate(
            addEditProductBundle.productId,
            addEditProductBundle.inStock ?: false
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.productUiState.collect {
                    when (it) {
                        ProductViewModel.ProductUiState.Success -> {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }

                        ProductViewModel.ProductUiState.Empty -> Unit
                        ProductViewModel.ProductUiState.Error -> Unit
                        ProductViewModel.ProductUiState.Loading -> Unit
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

    private fun saveProduct(productName: String, inStock: Boolean) {
        if (productName.isBlank()) return
        productViewModel.saveProduct(productName, inStock)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_edit_fragment_main, menu)
            }

            //NOTE: If you override onMenuItemSelected, OnSupportNavigateUp will only be called when returning false
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
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        (activity as AppCompatActivity?)!!.supportActionBar!!.title = when (editMode) {
            true -> getString(R.string.edit_product)
            false -> getString(R.string.add_product)
        }

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
        fun newInstance(name: String?, inStock: Boolean) =
            ProductFragment().apply {
                arguments = Bundler().makeAddProductBundle(name, inStock)
            }
    }
}