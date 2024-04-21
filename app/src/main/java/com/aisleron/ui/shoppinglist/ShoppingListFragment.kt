package com.aisleron.ui.shoppinglist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.google.android.material.textfield.TextInputEditText
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

    private fun updateProductStatus(
        item: ShoppingListItemViewModel,
        adapter: ShoppingListItemRecyclerViewAdapter,
        inStock: Boolean,
        absoluteAdapterPosition: Int
    ) {
        item.inStock = inStock
        viewModel.updateProductStatus(item)

        if ((viewModel.filterType == FilterType.IN_STOCK && !item.inStock) || (viewModel.filterType == FilterType.NEEDED && item.inStock)
        ) {
            viewModel.removeItem(item)
            adapter.notifyItemRemoved(absoluteAdapterPosition)
        } else {
            //TODO: Figure out why 'all' crashes the app after the second swipe.
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
                            updateProductStatus(
                                item,
                                view.adapter as ShoppingListItemRecyclerViewAdapter,
                                inStock,
                                absoluteAdapterPosition
                            )
                        }

                        override fun onProductMoved(item: ShoppingListItemViewModel) {
                            viewModel.updateProductRanks(item)
                        }

                        override fun onAisleMoved(item: ShoppingListItemViewModel) {
                            viewModel.updateAisleRanks(item)
                        }
                    }
                )

                val callback: ItemTouchHelper.Callback = ShoppingListItemMoveCallbackListener(
                    view.adapter as ShoppingListItemRecyclerViewAdapter
                )
                val touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(view)

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

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.shopping_list_fragment_main, menu)
            }

            //NOTE: If you override onMenuItemSelected, OnSupportNavigateUp will only be called when returning false
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.nav_add_aisle) {
                    addAisle(view.context)
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun addAisle(context: Context) {
        val inflater = requireActivity().layoutInflater
        val aisleDialogView = inflater.inflate(R.layout.dialog_aisle, null)
        val txtAisleName = aisleDialogView.findViewById<TextInputEditText>(R.id.edt_aisle_name)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder
            .setTitle(R.string.add_aisle)
            .setView(aisleDialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                viewModel.addAisle(txtAisleName.text.toString())
            }
            .setNegativeButton(R.string.cancel, null)

        val dialog: AlertDialog = builder.create()

        txtAisleName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }

        txtAisleName.requestFocus()
        dialog.show()
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