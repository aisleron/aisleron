package com.aisleron.ui.shoppinglist

import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aisleron.R
import com.aisleron.domain.model.Aisle
import com.aisleron.domain.model.FilterType
import com.aisleron.domain.model.Location
import com.aisleron.domain.model.Product
import com.aisleron.placeholder.LocationData
import com.aisleron.ui.productlist.ProductListItemRecyclerViewAdapter
import com.aisleron.ui.shoplist.ShopListItemRecyclerViewAdapter
import com.aisleron.widgets.ContextMenuRecyclerView

/**
 * A fragment representing a list of Items.
 */
class ShoppingListFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)
        val bundle = arguments
        val locationId : Int = bundle?.getInt("locationId") ?: 1
        val filterType : FilterType = if (bundle != null) bundle.get("filterType") as FilterType else FilterType.ALL
        val locationTitle : String? = bundle?.getString("locationTitle")
        if (locationTitle != null) {
            (activity as AppCompatActivity).supportActionBar?.title = locationTitle
        }

        // Set the adapter
        if (view is RecyclerView) {
            registerForContextMenu(view)
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = (LocationData.locations.filter{ l -> l.id == locationId })[0].aisles?.let {
                    ShoppingListItemRecyclerViewAdapter(
                        it.sortedWith(compareBy(Aisle::rank, Aisle::name)),
                        object :
                            ShoppingListItemRecyclerViewAdapter.ShoppingListItemListener {
                        }
                    )
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.product_list_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as ContextMenuRecyclerView.RecyclerViewContextMenuInfo

        return when (item.itemId) {
            R.id.nav_edit_product -> {
                Toast.makeText(
                    context,
                    "Edit Item ${info.position}",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ShoppingListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}