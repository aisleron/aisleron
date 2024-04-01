package com.aisleron.ui.shoppinglist

import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.aisleron.R
import com.aisleron.domain.model.FilterType
import com.aisleron.domain.model.LocationType
import com.aisleron.placeholder.ProductData
import com.aisleron.widgets.ContextMenuRecyclerView

/**
 * A fragment representing a list of [ShoppingListItemViewModel].
 */
class ShoppingListFragment : Fragment() {

    private lateinit var viewModel: ShoppingListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
        val locationId : Long = bundle?.getInt(ARG_LOCATION_ID)?.toLong() ?: 1
        val filterType : FilterType = if (bundle != null) bundle.getSerializable(ARG_FILTER_TYPE) as FilterType else FilterType.ALL

        viewModel = ShoppingListViewModel(locationId, filterType)
    }

    private fun updateProduct(item: ShoppingListItemViewModel) {
        if (item.inStock != null) {
            ProductData.products.find { p -> p.id == item.id }?.inStock = item.inStock == true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            view.addItemDecoration( DividerItemDecoration(context,  DividerItemDecoration.VERTICAL) )
            registerForContextMenu(view)

            with(view) {
               LinearLayoutManager(context)
                adapter = ShoppingListItemRecyclerViewAdapter(
                    viewModel.items,
                    object :
                        ShoppingListItemRecyclerViewAdapter.ShoppingListItemListener {
                        override fun onAisleClick(item: ShoppingListItemViewModel) {
                            Toast.makeText(
                                context,
                                "Aisle Click! Id: ${item.id}, Name: ${item.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onProductClick(item: ShoppingListItemViewModel) {
                            Toast.makeText(
                                context,
                                "Id: ${item.id}, Name: ${item.name}, In Stock: ${item.inStock}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onProductStatusChange(item: ShoppingListItemViewModel) {
                            updateProduct(item)

                            /*
                            Toast.makeText(
                                context,
                                "Checked Item Id: ${item.id}, Name: ${item.name}, In Stock: ${item.inStock}",
                                Toast.LENGTH_SHORT
                            ).show()

                             */
                        }

                    }
                )
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

        //Todo: Convert info type to ShoppingListItemType

        return when (item.itemId) {
            R.id.nav_edit_product -> {
                Toast.makeText(
                    context,
                    "Type: ${info.type} Edit Id: ${info.id}; Position ${info.position}",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onResume() {
        (activity as AppCompatActivity).supportActionBar?.title = when (viewModel.locationType) {
            LocationType.GENERIC -> when (viewModel.filterType){
                FilterType.INSTOCK -> resources.getString(R.string.menu_in_stock)
                FilterType.NEEDED -> resources.getString(R.string.menu_shopping_list)
                FilterType.ALL -> resources.getString(R.string.menu_all_items)
            }
            LocationType.SHOP -> viewModel.locationName
        }
        super.onResume()
    }

    companion object {

        const val ARG_LOCATION_ID = "locationId"
        const val ARG_FILTER_TYPE = "filterType"

        @JvmStatic
        fun newInstance(locationId: Long, filterType: FilterType) =
            ShoppingListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LOCATION_ID, locationId.toInt())
                    putSerializable (ARG_FILTER_TYPE, filterType)
                }
            }
    }
}