/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.ui.shoppinglist

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.AisleronFragment
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandler.FabClickedCallBack
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.loyaltycard.LoyaltyCardProvider
import com.aisleron.ui.settings.ShoppingListPreferences
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A fragment representing a list of [ShoppingListItem].
 */
class ShoppingListFragment(
    private val applicationTitleUpdateListener: ApplicationTitleUpdateListener,
    private val fabHandler: FabHandler,
    private val shoppingListPreferences: ShoppingListPreferences,
    private val loyaltyCardProvider: LoyaltyCardProvider
) : Fragment(), SearchView.OnQueryTextListener, ActionMode.Callback, FabClickedCallBack,
    MenuProvider, AisleronFragment {

    private var actionMode: ActionMode? = null
    private var actionModeItem: ShoppingListItem? = null
    private var actionModeItemView: View? = null
    private var editShopMenuItem: MenuItem? = null
    private var loyaltyCardMenuItem: MenuItem? = null

    private val shoppingListViewModel: ShoppingListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shoppingListBundle = Bundler().getShoppingListBundle(arguments)
        shoppingListViewModel.hydrate(
            shoppingListBundle.locationId,
            shoppingListBundle.filterType,
            shoppingListPreferences.showEmptyAisles(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initializeFab()

        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            setWindowInsetListeners(this, view, true, null)
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    shoppingListViewModel.shoppingListUiState.collect {
                        when (it) {
                            is ShoppingListViewModel.ShoppingListUiState.Error -> {
                                displayErrorSnackBar(
                                    it.errorCode,
                                    it.errorMessage,
                                    fabHandler.getFabView(requireActivity())
                                )
                            }

                            is ShoppingListViewModel.ShoppingListUiState.Updated -> {
                                updateTitle()
                                setMenuItemVisibility()

                                (view.adapter as ShoppingListItemRecyclerViewAdapter).submitList(
                                    it.shoppingList
                                )
                            }

                            else -> Unit
                        }
                    }
                }
            }

            with(view) {
                var touchHelper: ItemTouchHelper? = null

                LinearLayoutManager(context)
                adapter = ShoppingListItemRecyclerViewAdapter(
                    object :
                        ShoppingListItemRecyclerViewAdapter.ShoppingListItemListener {
                        override fun onClick(item: ShoppingListItem) {
                            actionMode?.finish()
                        }

                        override fun onProductStatusChange(
                            item: ProductShoppingListItem,
                            inStock: Boolean
                        ) {
                            actionMode?.finish()
                            shoppingListViewModel.updateProductStatus(item, inStock)
                            displayStatusChangeSnackBar(item, inStock)
                        }

                        override fun onListPositionChanged(
                            item: ShoppingListItem, precedingItem: ShoppingListItem?
                        ) {
                            actionMode?.finish()
                            shoppingListViewModel.updateItemRank(item, precedingItem)
                        }

                        override fun onLongClick(item: ShoppingListItem, view: View): Boolean {
                            // Finish the previous action mode and start a new one
                            actionMode?.finish()
                            if (item.itemType == ShoppingListItem.ItemType.EMPTY_LIST) {
                                return false
                            }

                            actionModeItem = item
                            actionModeItemView = view
                            actionModeItemView?.isSelected = true
                            return when (actionMode) {
                                null -> {
                                    // Start the CAB using the ActionMode.Callback defined earlier.
                                    actionMode =
                                        (requireActivity() as AppCompatActivity).startSupportActionMode(
                                            this@ShoppingListFragment
                                        )
                                    true
                                }

                                else -> false
                            }
                        }

                        override fun onMoved(item: ShoppingListItem) {
                            shoppingListViewModel.movedItem(item)
                        }

                        override fun onAisleExpandToggle(
                            item: AisleShoppingListItem, expanded: Boolean
                        ) {
                            actionMode?.finish()
                            shoppingListViewModel.updateAisleExpanded(item, expanded)
                        }

                        override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
                            touchHelper?.startDrag(viewHolder)
                        }

                        override fun onMove(item: ShoppingListItem) {}
                    }
                )

                val callback: ItemTouchHelper.Callback = ShoppingListItemMoveCallbackListener(
                    view.adapter as ShoppingListItemRecyclerViewAdapter
                )
                touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(view)
            }
        }
        return view
    }

    private fun setMenuItemVisibility() {
        editShopMenuItem?.isVisible = shoppingListViewModel.locationType == LocationType.SHOP
        loyaltyCardMenuItem?.isVisible = shoppingListViewModel.loyaltyCard != null
    }

    private fun displayStatusChangeSnackBar(item: ProductShoppingListItem, inStock: Boolean) {
        if (shoppingListPreferences.isStatusChangeSnackBarHidden(requireContext())) return

        val newStatus = getString(if (inStock) R.string.menu_in_stock else R.string.menu_needed)

        Snackbar.make(
            requireView(),
            getString(R.string.status_change_confirmation, item.name, newStatus),
            Snackbar.LENGTH_SHORT
        ).setAction(getString(R.string.undo)) { _ ->
            shoppingListViewModel.updateProductStatus(item, !inStock)
        }.setAnchorView(fabHandler.getFabView(this.requireActivity())).show()
    }

    private fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?, anchorView: View?
    ) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)

        ErrorSnackBar().make(
            requireView(),
            snackBarMessage,
            Snackbar.LENGTH_SHORT,
            anchorView
        ).show()
    }

    private fun initializeFab() {
        fabHandler.setFabOnClickedListener(this)
        fabHandler.setFabItems(
            this.requireActivity(),
            FabHandler.FabOption.ADD_SHOP,
            FabHandler.FabOption.ADD_AISLE,
            FabHandler.FabOption.ADD_PRODUCT
        )

        fabHandler.setFabOnClickListener(this.requireActivity(), FabHandler.FabOption.ADD_PRODUCT) {
            navigateToAddProduct(shoppingListViewModel.defaultFilter)
        }

        fabHandler.setFabOnClickListener(this.requireActivity(), FabHandler.FabOption.ADD_AISLE) {
            showAisleDialog(requireView().context)
        }
    }

    private fun updateTitle() {
        val appTitle =
            when (shoppingListViewModel.locationType) {
                LocationType.HOME ->
                    when (shoppingListViewModel.defaultFilter) {
                        FilterType.IN_STOCK -> resources.getString(R.string.menu_in_stock)
                        FilterType.NEEDED -> resources.getString(R.string.menu_needed)
                        FilterType.ALL -> resources.getString(R.string.menu_all_items)
                    }

                LocationType.SHOP -> shoppingListViewModel.locationName
            }

        applicationTitleUpdateListener.applicationTitleUpdated(requireActivity(), appTitle)
    }

    private fun navigateToAddProduct(filterType: FilterType, aisleId: Int? = null) {
        val bundle =
            Bundler().makeAddProductBundle(
                locationId = shoppingListViewModel.locationId,
                name = null,
                inStock = filterType == FilterType.IN_STOCK,
                aisleId = aisleId
            )

        this.findNavController().navigate(R.id.nav_add_product, bundle)
    }

    private fun navigateToEditProduct(productId: Int) {
        val bundle = Bundler().makeEditProductBundle(
            productId = productId, locationId = shoppingListViewModel.locationId
        )

        this.findNavController().navigate(R.id.nav_add_product, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateToEditShop(locationId: Int) {
        val bundle = Bundler().makeEditLocationBundle(locationId)
        this.findNavController().navigate(R.id.nav_add_shop, bundle)
    }

    private fun showAisleDialog(context: Context, aisle: AisleShoppingListItem? = null) {
        val inflater = requireActivity().layoutInflater
        val aisleDialogView = inflater.inflate(R.layout.dialog_aisle, null)
        val txtAisleName = aisleDialogView.findViewById<TextInputEditText>(R.id.edt_aisle_name)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder
            .setView(aisleDialogView)
            .setNeutralButton(android.R.string.cancel, null)

        if (aisle == null) {
            //Add a new Aisle
            builder
                .setTitle(R.string.add_aisle)
                .setNegativeButton(R.string.add_another) { _, _ ->
                    addNewAisle(txtAisleName.text.toString())
                    showAisleDialog(context)
                }
                .setPositiveButton(R.string.done) { _, _ -> addNewAisle(txtAisleName.text.toString()) }
        } else {
            //Edit an Aisle
            txtAisleName.setText(aisle.name)
            builder
                .setTitle(R.string.edit_aisle)
                .setPositiveButton(R.string.done) { _, _ ->
                    updateAisleName(aisle, txtAisleName.text.toString())
                }
        }

        val dialog: AlertDialog = builder.create()

        txtAisleName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }

        txtAisleName.requestFocus()
        dialog.show()
    }

    private fun updateAisleName(aisle: AisleShoppingListItem, newName: String) {
        if (newName.isNotBlank()) {
            shoppingListViewModel.updateAisleName(aisle, newName)
        }
    }

    private fun addNewAisle(aisleName: String) {
        if (aisleName.isNotBlank()) {
            shoppingListViewModel.addAisle(aisleName)
        }
    }

    private fun confirmDelete(context: Context, item: ShoppingListItem) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setTitle(getString(R.string.delete_confirmation, item.name))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                shoppingListViewModel.removeItem(item)
            }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }

    private fun editShoppingListItem(item: ShoppingListItem) {
        when (item) {
            is AisleShoppingListItem -> showAisleDialog(requireContext(), item)
            is ProductShoppingListItem -> navigateToEditProduct(item.id)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        shoppingListViewModel.submitProductSearch(productNameQuery = newText ?: "")
        return false
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

        menu.findItem(R.id.mnu_add_product_to_aisle)
            .setVisible(actionModeItem?.itemType == ShoppingListItem.ItemType.AISLE)

        return false // Return false if nothing is done
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        var result = true
        when (item.itemId) {
            R.id.mnu_edit_shopping_list_item ->
                actionModeItem?.let { editShoppingListItem(it) }

            R.id.mnu_delete_shopping_list_item ->
                actionModeItem?.let { confirmDelete(requireContext(), it) }

            R.id.mnu_add_product_to_aisle ->
                actionModeItem?.let {
                    navigateToAddProduct(
                        shoppingListViewModel.defaultFilter, it.aisleId
                    )
                }

            else -> result = false // No action picked, so don't close the CAB.
        }

        if (result) mode.finish()  // Action picked, so close the CAB.

        return result
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionModeItemView?.isSelected = false
        actionMode = null
        actionModeItem = null
        actionModeItemView = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.shopping_list_fragment_main, menu)

        val searchManager =
            getSystemService(requireContext(), SearchManager::class.java) as SearchManager
        val searchableInfo =
            searchManager.getSearchableInfo(requireActivity().componentName)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setMaxWidth(Integer.MAX_VALUE)
        searchView.setSearchableInfo(searchableInfo)
        searchView.setOnQueryTextListener(this@ShoppingListFragment)
        searchView.setOnCloseListener {
            shoppingListViewModel.requestDefaultList()
            false
        }

        //OnAttachStateChange is here as a workaround because OnCloseListener doesn't fire
        searchView.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                shoppingListViewModel.requestDefaultList()
            }
        })

        editShopMenuItem = menu.findItem(R.id.mnu_edit_shop)
        loyaltyCardMenuItem = menu.findItem(R.id.mnu_show_loyalty_card)
        setMenuItemVisibility()
    }

    //NOTE: If you override onMenuItemSelected, OnSupportNavigateUp will only be called when returning false
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.mnu_edit_shop -> {
                navigateToEditShop(locationId = shoppingListViewModel.locationId)
                true
            }

            R.id.mnu_sort_list_by_name -> {
                confirmSort(requireContext())
                true
            }

            R.id.mnu_show_loyalty_card -> {
                shoppingListViewModel.loyaltyCard?.let { showLoyaltyCard(it) }
                true
            }

            else -> false
        }
    }

    private fun showLoyaltyCard(loyaltyCard: LoyaltyCard) {
        try {
            loyaltyCardProvider.displayLoyaltyCard(requireContext(), loyaltyCard)
        } catch (e: AisleronException.LoyaltyCardProviderException) {
            loyaltyCardProvider.getNotInstalledDialog(requireContext()).show()
        } catch (e: Exception) {
            displayErrorSnackBar(
                AisleronException.ExceptionCode.GENERIC_EXCEPTION,
                e.message,
                fabHandler.getFabView(this.requireActivity())
            )
        }
    }

    private fun confirmSort(context: Context) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setTitle(getString(R.string.sort_confirm_title))
            .setMessage(R.string.sort_confirm_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                shoppingListViewModel.sortListByName()
            }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }

    companion object {

        private const val ARG_LOCATION_ID = "locationId"
        private const val ARG_FILTER_TYPE = "filterType"

        @JvmStatic
        fun newInstance(
            locationId: Long,
            filterType: FilterType,
            applicationTitleUpdateListener: ApplicationTitleUpdateListener,
            fabHandler: FabHandler,
            shoppingListPreferences: ShoppingListPreferences,
            loyaltyCardProvider: LoyaltyCardProvider
        ) =
            ShoppingListFragment(
                applicationTitleUpdateListener,
                fabHandler,
                shoppingListPreferences,
                loyaltyCardProvider
            ).apply {
                arguments = Bundle().apply {
                    putInt(ARG_LOCATION_ID, locationId.toInt())
                    putSerializable(ARG_FILTER_TYPE, filterType)
                }
            }
    }

    override fun fabClicked(fabOption: FabHandler.FabOption) {
        actionMode?.finish()
    }
}