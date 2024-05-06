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
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aisleron.R
import com.aisleron.databinding.FragmentShopBinding
import com.aisleron.ui.bundles.AddEditLocationBundle
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.shoppinglist.ShoppingListFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ShopFragment : Fragment() {

    private val shopViewModel: ShopViewModel by viewModel()
    private var _binding: FragmentShopBinding? = null

    private val binding get() = _binding!!

    private var editMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val addEditLocationBundle = Bundler().getAddEditLocationBundle(arguments)
        editMode = addEditLocationBundle.actionType == AddEditLocationBundle.LocationAction.EDIT

        shopViewModel.hydrate(addEditLocationBundle.locationId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                shopViewModel.shopUiState.collect {
                    when (it) {
                        ShopViewModel.ShopUiState.Success -> {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }

                        ShopViewModel.ShopUiState.Empty -> Unit
                        ShopViewModel.ShopUiState.Error -> Unit
                        ShopViewModel.ShopUiState.Loading -> Unit
                        is ShopViewModel.ShopUiState.Updated -> {
                            binding.edtShopName.setText(shopViewModel.locationName)
                            binding.swcShopPinned.isChecked = shopViewModel.pinned
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun saveShop(shopName: String, pinned: Boolean) {
        if (shopName.isBlank()) return
        shopViewModel.saveLocation(shopName, pinned)
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
                        saveShop(
                            binding.edtShopName.text.toString(),
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
        super.onResume()

        (activity as AppCompatActivity?)!!.supportActionBar!!.title = when (editMode) {
            true -> getString(R.string.edit_location)
            false -> getString(R.string.add_location)
        }

        val edtLocationName = binding.edtShopName
        edtLocationName.postDelayed({
            edtLocationName.requestFocus()
            val imm =
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(edtLocationName, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    companion object {

        @JvmStatic
        fun newInstance(name: String?) =
            ShoppingListFragment().apply {
                arguments = Bundler().makeAddLocationBundle(name)
            }
    }
}