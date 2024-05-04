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
import com.aisleron.domain.FilterType
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductFragment : Fragment(){

    private val productViewModel: ProductViewModel by viewModel()
    private var _binding: FragmentProductBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val productId: Int = it.getInt(ARG_PRODUCT_ID)
            val filterType: FilterType = it.getSerializable(ARG_FILTER_TYPE) as FilterType
            productViewModel.hydrate(productId, filterType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)

        val chk = binding.chkProductInStock
        chk.setOnClickListener { chk.isChecked = !chk.isChecked }
        return binding.root
    }

    private fun saveProduct(productName: String, inStock: Boolean) {
        if (productName.isBlank()) return

        productViewModel.saveProduct(productName, inStock)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.productUiState.collect {
                    when (it) {
                        is ProductViewModel.ProductUiState.Success -> {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }

                        ProductViewModel.ProductUiState.Empty -> Unit
                        ProductViewModel.ProductUiState.Error -> Unit
                        ProductViewModel.ProductUiState.Loading -> Unit
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chkProductInStock.isChecked = productViewModel.inStock

        val edtProductName = binding.edtProductName
        edtProductName.setText(productViewModel.productName)

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
                            edtProductName.text.toString(),
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
        if (productViewModel.productName != null) {
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = productViewModel.productName
        }
        super.onResume()

        val edtProductName = binding.edtProductName
        edtProductName.postDelayed({
            edtProductName.requestFocus()
            val imm =
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(edtProductName, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    companion object {

        private const val ARG_PRODUCT_ID = "productId"
        private const val ARG_FILTER_TYPE = "filterType"

        fun newInstance(productId: Int, filterType: FilterType) =
            ProductFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUCT_ID, productId)
                    putSerializable(ARG_FILTER_TYPE, filterType)
                }
            }
    }
}