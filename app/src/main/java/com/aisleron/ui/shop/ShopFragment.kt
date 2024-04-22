package com.aisleron.ui.shop

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
import com.aisleron.databinding.FragmentShopBinding
import com.aisleron.ui.shoppinglist.ShoppingListFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ShopFragment : Fragment() {

    private val shopViewModel: ShopViewModel by viewModel()
    private var _binding: FragmentShopBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            shopViewModel.hydrate(it.getInt(ARG_LOCATION_ID))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        val button: Button = binding.btnSaveShop
        button.setOnClickListener {
            shopViewModel.saveLocation(
                binding.edtShopName.text.toString(),
                binding.swcShopPinned.isChecked
            )

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    shopViewModel.shopUiState.collect {
                        when (it) {
                            is ShopViewModel.ShopUiState.Success -> {
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }

                            ShopViewModel.ShopUiState.Empty -> Unit
                            ShopViewModel.ShopUiState.Error -> Unit
                            ShopViewModel.ShopUiState.Loading -> Unit
                        }
                    }
                }
            }


        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtShopName.setText(shopViewModel.locationName)
        binding.swcShopPinned.isChecked = shopViewModel.pinned ?: true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        if (shopViewModel.locationName != null) {
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = shopViewModel.locationName
        }
        super.onResume()
    }

    companion object {

        private const val ARG_LOCATION_ID = "locationId"

        @JvmStatic
        fun newInstance(locationId: Int) =
            ShoppingListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LOCATION_ID, locationId)
                }
            }
    }
}