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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.widgets.ContextMenuRecyclerView
import org.koin.android.ext.android.inject

/**
 * A fragment representing a list of [ShoppingListItemViewModel].
 */
class ShoppingListFragment : Fragment() {

    private val viewModel: ShoppingListViewModel by inject<ShoppingListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
        val locationId: Int = bundle?.getInt(ARG_LOCATION_ID) ?: 1
        val filterType: FilterType =
            if (bundle != null) bundle.getSerializable(ARG_FILTER_TYPE) as FilterType else FilterType.ALL

        viewModel.hydrate(locationId, filterType)
    }

    private fun updateProduct(
        item: ShoppingListItemViewModel,
        adapter: ShoppingListItemRecyclerViewAdapter,
        inStock: Boolean,
        absoluteAdapterPosition: Int
    ) {
        item.inStock = inStock
        viewModel.updateProductStatus(item)

        if ((viewModel.filterType == FilterType.IN_STOCK && item.inStock == false)
            || (viewModel.filterType == FilterType.NEEDED && item.inStock == true)
        ) {
            viewModel.removeItem(item)
            adapter.notifyItemRemoved(absoluteAdapterPosition)
        } else {

            adapter.notifyItemChanged(absoluteAdapterPosition)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            view.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            registerForContextMenu(view)
            //TODO: Refresh the data when view is reloaded, e.g. after adding a new product

            lifecycleScope.launchWhenStarted {
                viewModel.shoppingListUiState.collect {
                    when (it) {
                        is ShoppingListViewModel.ShoppingListUiState.Success -> {
                            view.adapter?.notifyDataSetChanged()
                            updateTitle()
                        }

                        ShoppingListViewModel.ShoppingListUiState.Empty -> Unit
                        ShoppingListViewModel.ShoppingListUiState.Loading -> Unit
                    }
                }
            }

            with(view) {
                LinearLayoutManager(context)
                adapter = ShoppingListItemRecyclerViewAdapter(
                    viewModel.items,
                    object :
                        ShoppingListItemRecyclerViewAdapter.ShoppingListItemListener {
                        override fun onAisleClick(item: ShoppingListItemViewModel) {
                            Toast.makeText(
                                context,
                                "AisleEntity Click! Id: ${item.id}, Name: ${item.name}",
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

                        override fun onProductStatusChange(
                            item: ShoppingListItemViewModel,
                            inStock: Boolean,
                            absoluteAdapterPosition: Int
                        ) {
                            updateProduct(
                                item,
                                view.adapter as ShoppingListItemRecyclerViewAdapter,
                                inStock,
                                absoluteAdapterPosition
                            )
                        }
                    }
                )
            }
        }
        return view
    }

    private fun updateTitle() {
        (activity as AppCompatActivity).supportActionBar?.title = when (viewModel.locationType) {
            LocationType.HOME ->
                when (viewModel.filterType) {
                    FilterType.IN_STOCK -> resources.getString(R.string.menu_in_stock)
                    FilterType.NEEDED -> resources.getString(R.string.menu_shopping_list)
                    FilterType.ALL -> resources.getString(R.string.menu_all_items)
                }

            LocationType.SHOP -> viewModel.locationName
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
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

    companion object {

        private const val ARG_LOCATION_ID = "locationId"
        private const val ARG_FILTER_TYPE = "filterType"

        @JvmStatic
        fun newInstance(locationId: Long, filterType: FilterType) =
            ShoppingListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LOCATION_ID, locationId.toInt())
                    putSerializable(ARG_FILTER_TYPE, filterType)
                }
            }
    }
}