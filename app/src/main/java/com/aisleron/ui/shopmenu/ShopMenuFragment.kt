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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.ui.shoplist.ShopListItemViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A fragment representing a list of Items.
 */
class ShopMenuFragment : Fragment() {

    private var columnCount = 1
    private val shopListViewModel: ShopListViewModel by viewModel()

    private val items = mutableListOf<ShopListItemViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        shopListViewModel.hydratePinnedShops()
    }

    private fun navigateToShoppingList(item: ShopListItemViewModel) {
        val bundle = Bundle()
        bundle.putInt(ARG_LOCATION_ID, item.id)
        bundle.putSerializable(ARG_FILTER_TYPE, item.defaultFilter)
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
                            ShopListViewModel.ShopListUiState.Empty -> Unit
                            ShopListViewModel.ShopListUiState.Loading -> Unit
                            ShopListViewModel.ShopListUiState.Error -> Unit
                            ShopListViewModel.ShopListUiState.Success -> Unit

                            is ShopListViewModel.ShopListUiState.Updated -> {
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
                    ShopMenuRecyclerViewAdapter(items,
                        object :
                            ShopMenuRecyclerViewAdapter.ShopMenuItemListener {
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
        fun newInstance(columnCount: Int, locationId: Long, filterType: FilterType) =
            ShopMenuFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putInt(ARG_LOCATION_ID, locationId.toInt())
                    putSerializable(ARG_FILTER_TYPE, filterType)
                }
            }
    }
}