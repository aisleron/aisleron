package com.aisleron.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
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

        return binding.root
    }

    private fun saveShop(shopName: String, pinned: Boolean) {
        if (shopName.isBlank()) return

        shopViewModel.saveLocation(shopName, pinned)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swcShopPinned.isChecked = shopViewModel.pinned ?: true

        val edtShopName = binding.edtShopName
        edtShopName.setText(shopViewModel.locationName)
        edtShopName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val imm = getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(edtShopName, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        edtShopName.requestFocus()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_edit_fragment_main, menu)
            }

            //NOTE: If you override onMenuItemSelected, OnSupportNavigateUp will only be called when returning false
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mnu_btn_save -> {
                        saveShop(
                            edtShopName.text.toString(),
                            binding.swcShopPinned.isChecked
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