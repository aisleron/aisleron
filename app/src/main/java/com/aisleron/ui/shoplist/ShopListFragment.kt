package com.aisleron.ui.shoplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import com.aisleron.ui.bundles.Bundler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A fragment representing a list of Items.
 */
class ShopListFragment : Fragment() {

    private var columnCount = 3
    private val shopListViewModel: ShopListViewModel by viewModel()

    private val items = mutableListOf<ShopListItemViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        shopListViewModel.hydrateAllShops()
    }

    private fun navigateToShoppingList(item: ShopListItemViewModel) {
        val bundle = Bundle()
        bundle.putInt(ARG_LOCATION_ID, item.id)
        bundle.putSerializable(ARG_FILTER_TYPE, item.defaultFilter)
        this.findNavController().navigate(R.id.action_nav_all_shops_to_nav_shopping_list, bundle)
    }

    private fun navigateToAddShop() {
        val bundle = Bundler().makeAddLocationBundle()
        this.findNavController().navigate(R.id.nav_add_shop, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fab = this.activity?.findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources, R.drawable.baseline_add_business_24, context?.theme
            )
        )
        fab.setOnClickListener { _ ->
            navigateToAddShop()
        }

        val view = inflater.inflate(R.layout.fragment_shop_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    shopListViewModel.shopListUiState.collect {
                        when (it) {
                            ShopListViewModel.ShopListUiState.Empty -> Unit
                            ShopListViewModel.ShopListUiState.Loading -> Unit
                            ShopListViewModel.ShopListUiState.Error -> Unit
                            is ShopListViewModel.ShopListUiState.Success -> {
                                items.clear()
                                items += it.shops
                                view.adapter?.notifyDataSetChanged()
                            }

                        }
                    }
                }
            }
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter =
                    ShopListItemRecyclerViewAdapter(items,
                        object :
                            ShopListItemRecyclerViewAdapter.ShopListItemListener {
                            override fun onItemClick(item: ShopListItemViewModel) {
                                navigateToShoppingList(item)
                            }
                        })
            }
        }
        return view
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_LOCATION_ID = "locationId"
        const val ARG_FILTER_TYPE = "filterType"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            ShopListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}