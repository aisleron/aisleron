package com.aisleron.ui.navshoplist

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
import com.aisleron.domain.model.Location
import com.aisleron.domain.model.LocationType
import com.aisleron.placeholder.LocationData

/**
 * A fragment representing a list of Items.
 */
class NavShopListFragment : Fragment() {

    private var columnCount = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    private fun navigateToShoppingList(item: Location) {
        val bundle = Bundle()
        bundle.putInt("locationId", item.id)
        bundle.putSerializable("filterType", item.defaultFilter)
        bundle.putString("locationTitle", item.name)
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
                adapter = NavShopListRecyclerViewAdapter(LocationData.locations.filter { s -> s.type == LocationType.SHOP } , object :
                    NavShopListRecyclerViewAdapter.NavListShopItemListener {
                    override fun onItemClick(item: Location) {
                        navigateToShoppingList(item)
                    }
                })

            }
        }
        return view
    }



    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            NavShopListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}