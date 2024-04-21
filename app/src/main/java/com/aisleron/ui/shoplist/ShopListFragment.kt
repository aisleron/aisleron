package com.aisleron.ui.shoplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * A fragment representing a list of Items.
 */
class ShopListFragment : Fragment() {

    private var columnCount = 3
    private val viewModel: ShopListViewModel by inject<ShopListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        viewModel.hydrateAllShops()
    }

    private fun navigateToShoppingList(item: ShopListItemViewModel) {
        val bundle = Bundle()
        bundle.putInt(ARG_LOCATION_ID, item.id)
        bundle.putSerializable(ARG_FILTER_TYPE, item.defaultFilter)
        this.findNavController().navigate(R.id.action_nav_all_shops_to_nav_shopping_list, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.shopListUiState.collect {
                        when (it) {
                            is ShopListViewModel.ShopListUiState.Success -> view.adapter?.notifyDataSetChanged()
                            ShopListViewModel.ShopListUiState.Empty -> Unit
                            ShopListViewModel.ShopListUiState.Loading -> Unit
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
                    ShopListItemRecyclerViewAdapter(viewModel.shops,
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