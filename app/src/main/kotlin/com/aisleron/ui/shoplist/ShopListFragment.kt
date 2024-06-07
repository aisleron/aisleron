package com.aisleron.ui.shoplist

import android.content.Context
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A fragment representing a list of Items.
 */
class ShopListFragment : Fragment(), ActionMode.Callback {

    private var actionMode: ActionMode? = null
    private var actionModeItem: ShopListItemViewModel? = null


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

    private fun navigateToEditShop(locationId: Int) {
        val bundle = Bundler().makeEditLocationBundle(locationId)
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
                            ShopListViewModel.ShopListUiState.Success -> Unit
                            is ShopListViewModel.ShopListUiState.Error -> {
                                displayErrorSnackBar(it.errorCode, it.errorMessage)
                            }

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
                    ShopListItemRecyclerViewAdapter(items,
                        object :
                            ShopListItemRecyclerViewAdapter.ShopListItemListener {
                            override fun onClick(item: ShopListItemViewModel) {
                                navigateToShoppingList(item)
                            }

                            override fun onLongClick(item: ShopListItemViewModel): Boolean {
                                actionModeItem = item
                                return when (actionMode) {
                                    null -> {
                                        // Start the CAB using the ActionMode.Callback defined earlier.
                                        actionMode =
                                            requireActivity().startActionMode(this@ShopListFragment)
                                        true
                                    }

                                    else -> false
                                }
                            }
                        })
            }
        }
        return view
    }

    private fun displayErrorSnackBar(errorCode: String, errorMessage: String?) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)
        ErrorSnackBar().make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        // Inflate a menu resource providing context menu items.
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.shopping_list_fragment_context, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.title = actionModeItem?.name
        menu.findItem(R.id.mnu_delete_shopping_list_item)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return false // Return false if nothing is done
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_edit_shopping_list_item -> {
                actionModeItem?.let { editShopListItem(it) }
                mode.finish()
                true // Action picked, so close the CAB.
            }

            R.id.mnu_delete_shopping_list_item -> {
                actionModeItem?.let { confirmDelete(requireContext(), it) }
                mode.finish()
                true
            }

            else -> false
        }
    }

    private fun confirmDelete(context: Context, item: ShopListItemViewModel) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder
            .setTitle(getString(R.string.delete_confirmation, item.name))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                shopListViewModel.removeItem(item)
            }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }

    private fun editShopListItem(item: ShopListItemViewModel) {
        navigateToEditShop(item.id)
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode = null
        actionModeItem = null
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