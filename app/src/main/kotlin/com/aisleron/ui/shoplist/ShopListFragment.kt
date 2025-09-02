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

package com.aisleron.ui.shoplist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.AisleronFragment
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandler.FabClickedCallBack
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.copyentity.CopyEntityDialogFragment
import com.aisleron.ui.copyentity.CopyEntityType
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A fragment representing a list of Items.
 */
class ShopListFragment(private val fabHandler: FabHandler) : Fragment(), ActionMode.Callback,
    ShopListItemRecyclerViewAdapter.ShopListItemListener, FabClickedCallBack, AisleronFragment {

    private var actionMode: ActionMode? = null
    private var actionModeItem: ShopListItemViewModel? = null
    private var actionModeItemView: View? = null

    private var columnCount = 3
    private val shopListViewModel: ShopListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        shopListViewModel.hydrateAllShops()
    }

    private fun navigateToShoppingList(item: ShopListItemViewModel) {
        val bundle = Bundler().makeShoppingListBundle(item.id, item.defaultFilter)
        this.findNavController().navigate(R.id.action_nav_all_shops_to_nav_shopping_list, bundle)
    }

    private fun navigateToEditShop(locationId: Int) {
        val bundle = Bundler().makeEditLocationBundle(locationId)
        this.findNavController().navigate(R.id.nav_add_shop, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fabHandler.setFabItems(this.requireActivity(), FabHandler.FabOption.ADD_SHOP)
        fabHandler.setFabOnClickedListener(this)

        val view = inflater.inflate(R.layout.fragment_shop_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            setWindowInsetListeners(this, view, true, R.dimen.text_margin)
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    shopListViewModel.shopListUiState.collect {
                        when (it) {
                            is ShopListViewModel.ShopListUiState.Error -> {
                                displayErrorSnackBar(it.errorCode, it.errorMessage)
                            }

                            is ShopListViewModel.ShopListUiState.Updated -> {
                                (view.adapter as ShopListItemRecyclerViewAdapter).submitList(it.shops)
                            }

                            else -> Unit
                        }
                    }
                }
            }
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = ShopListItemRecyclerViewAdapter(this@ShopListFragment)
            }
        }
        return view
    }

    private fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?
    ) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)
        ErrorSnackBar().make(
            requireView(),
            snackBarMessage,
            Snackbar.LENGTH_SHORT,
            fabHandler.getFabView(this.requireActivity())
        ).show()
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        // Inflate a menu resource providing context menu items.
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.shop_list_fragment_context, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.title = actionModeItem?.name
        menu.findItem(R.id.mnu_delete_shop_list_item)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return false // Return false if nothing is done
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        var result = true
        when (item.itemId) {
            R.id.mnu_edit_shop_list_item ->
                actionModeItem?.let { editShopListItem(it) }

            R.id.mnu_delete_shop_list_item ->
                actionModeItem?.let { confirmDelete(requireContext(), it) }

            R.id.mnu_copy_shop_list_item ->
                actionModeItem?.let { showCopyLocationDialog(it) }

            else -> result = false
        }

        if (result) mode.finish()  // Action picked, so close the CAB.

        return result
    }

    private fun showCopyLocationDialog(item: ShopListItemViewModel) {
        val dialog = CopyEntityDialogFragment.newInstance(
            type = CopyEntityType.Location(item.id),
            title = getString(R.string.copy_entity_title, item.name),
            defaultName = "${item.name} (${getString(android.R.string.copy)})",
            nameHint = getString(R.string.new_location_name)
        )

        dialog.onCopySuccess = {
            requireView().postDelayed({
                Snackbar.make(
                    requireView(),
                    getString(R.string.entity_copied, item.name),
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(fabHandler.getFabView(this.requireActivity()))
                    .show()
            }, 250)
        }

        dialog.show(childFragmentManager, "copyDialog")
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
        actionModeItemView?.isSelected = false
        actionMode = null
        actionModeItem = null
        actionModeItemView = null
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int, fabHandler: FabHandler) =
            ShopListFragment(fabHandler).apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    override fun onClick(item: ShopListItemViewModel) {
        actionMode?.let {
            it.finish()
            return
        }

        navigateToShoppingList(item)
    }

    override fun onLongClick(item: ShopListItemViewModel, view: View): Boolean {
        actionMode?.finish()
        actionModeItem = item
        actionModeItemView = view
        actionModeItemView?.isSelected = true
        return when (actionMode) {
            null -> {
                // Start the CAB using the ActionMode.Callback defined earlier.
                actionMode =
                    (requireActivity() as AppCompatActivity).startSupportActionMode(this@ShopListFragment)
                true
            }

            else -> false
        }
    }

    override fun fabClicked(fabOption: FabHandler.FabOption) {
        actionMode?.finish()
    }
}