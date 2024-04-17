package com.aisleron.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aisleron.databinding.FragmentProductBinding
import org.koin.android.ext.android.inject

class ProductFragment : Fragment() {

    private val viewModel: ProductViewModel by inject<ProductViewModel>()
    private var _binding: FragmentProductBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            viewModel.hydrate(it.getInt(ARG_PRODUCT_ID))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        val button: Button = binding.btnSaveProduct
        button.setOnClickListener {
            viewModel.saveProduct(
                binding.edtProductName.text.toString(),
                binding.chkProductInStock.isChecked
            )
            lifecycleScope.launchWhenStarted {
                viewModel.productUiState.collect {
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

        val chk = binding.chkProductInStock
        chk.setOnClickListener { chk.isChecked = !chk.isChecked }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtProductName.setText(viewModel.productName)
        binding.chkProductInStock.isChecked = viewModel.inStock ?: true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        if (viewModel.productName != null) {
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = viewModel.productName
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