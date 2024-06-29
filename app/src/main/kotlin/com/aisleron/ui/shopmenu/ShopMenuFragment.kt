package com.aisleron.ui.shopmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.shoplist.ShopListItemViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A fragment representing a list of Items.
 */
class ShopMenuFragment : Fragment(), ShopMenuRecyclerViewAdapter.ShopMenuItemListener {
    private val shopListViewModel: ShopListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopListViewModel.hydratePinnedShops()
    }

    private fun navigateToShoppingList(item: ShopListItemViewModel) {
        val bundle = Bundler().makeShoppingListBundle(item.id, item.defaultFilter)
        this.findNavController().navigate(R.id.nav_shopping_list, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop_menu, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    shopListViewModel.shopListUiState.collect {
                        when (it) {
                            is ShopListViewModel.ShopListUiState.Updated -> {
                                (view.adapter as ShopMenuRecyclerViewAdapter).submitList(it.shops)
                            }

                            else -> Unit
                        }
                    }
                }
            }
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ShopMenuRecyclerViewAdapter(this@ShopMenuFragment)
            }
        }
        return view
    }

    companion object {

        @JvmStatic
        fun newInstance() = ShopMenuFragment().apply {
            arguments = null
        }
    }

    override fun onClick(item: ShopListItemViewModel) {
        navigateToShoppingList(item)
    }
}