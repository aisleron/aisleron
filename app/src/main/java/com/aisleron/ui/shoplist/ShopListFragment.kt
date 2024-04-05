package com.aisleron.ui.shoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.aisleron.R
import com.aisleron.domain.model.FilterType
import com.aisleron.domain.model.Location
import com.aisleron.domain.model.LocationType
import com.aisleron.placeholder.LocationData

/**
 * A fragment representing a list of Items.
 */
class ShopListFragment : Fragment() {

    private var columnCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    private fun navigateToShoppingList(item: Location) {
        val bundle = Bundle()
        bundle.putInt(ARG_LOCATION_ID, item.id.toInt())
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
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter =
                    ShopListItemRecyclerViewAdapter(LocationData.locations.filter { s -> s.type == LocationType.SHOP },
                        object :
                            ShopListItemRecyclerViewAdapter.ShopListItemListener {
                            override fun onItemClick(item: Location) {
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
            ShopListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putInt(ARG_LOCATION_ID, locationId.toInt())
                    putSerializable(ARG_FILTER_TYPE, filterType)
                }
            }
    }
}