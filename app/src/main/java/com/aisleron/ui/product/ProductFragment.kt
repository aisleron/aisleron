package com.aisleron.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.databinding.FragmentProductBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductFragment : Fragment() {

    private val productViewModel: ProductViewModel by viewModel()
    private var _binding: FragmentProductBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            productViewModel.hydrate(it.getInt(ARG_PRODUCT_ID))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        val button: Button = binding.btnSaveProduct
        button.setOnClickListener {
            productViewModel.saveProduct(
                binding.edtProductName.text.toString(),
                binding.chkProductInStock.isChecked
            )
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

        val chk = binding.chkProductInStock
        chk.setOnClickListener { chk.isChecked = !chk.isChecked }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtProductName.setText(productViewModel.productName)
        binding.chkProductInStock.isChecked = productViewModel.inStock ?: true
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
    }

    companion object {

        private const val ARG_PRODUCT_ID = "productId"
        fun newInstance(productId: Int) =
            ProductFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUCT_ID, productId)
                }
            }
    }
}