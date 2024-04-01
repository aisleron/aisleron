package com.aisleron.ui.product

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.aisleron.databinding.FragmentProductBinding
import com.aisleron.domain.model.Product
import com.aisleron.placeholder.ProductData


class ProductFragment : Fragment() {

    companion object {
        fun newInstance() = ProductFragment()
    }

    private val viewModel: ProductViewModel by viewModels()
    private var _binding: FragmentProductBinding? =null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        val button: Button = binding.btnSaveProduct
        button.setOnClickListener{
            ProductData.products.add(
                Product(
                    id = (ProductData.products.size + 1).toLong(),
                    name = binding.edtProductName.text.toString(),
                    inStock = binding.chkProductInStock.isChecked
                ),
            )
            requireActivity().onBackPressedDispatcher.onBackPressed ()
        }

        val chk = binding.chkProductInStock
        chk.setOnClickListener{
           chk.isChecked = !chk.isChecked
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}