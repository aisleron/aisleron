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
import com.aisleron.model.Location
import com.aisleron.model.LocationType
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

    private fun navigateToShop(item: Location) {
        val bundle = Bundle()
        bundle.putSerializable("key", item)
        findNavController().navigate(R.id.navigate_to_shop, bundle)
            
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
                adapter = ShopListItemRecyclerViewAdapter(LocationData.locations.filter { s -> s.type == LocationType.SHOP } , object :
                    ShopListItemRecyclerViewAdapter.ShopListItemListener {
                    override fun onItemClick(item: Location) {
                        navigateToShop(item)
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
            ShopListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}