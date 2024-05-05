package com.aisleron.ui.shoppinglist

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.ActionMode
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
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.MenuHost
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
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.FabHandler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * A fragment representing a list of [ShoppingListItemViewModel].
 */
class ShoppingListFragment : Fragment(), SearchView.OnQueryTextListener, ActionMode.Callback {

    private var actionMode: ActionMode? = null
    private var actionModeItem: ShoppingListItemViewModel? = null

    private val shoppingListViewModel: ShoppingListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
        val locationId: Int = bundle?.getInt(ARG_LOCATION_ID) ?: 1
        val filterType: FilterType =
            if (bundle != null) bundle.getSerializable(ARG_FILTER_TYPE) as FilterType else FilterType.ALL

        shoppingListViewModel.hydrate(locationId, filterType)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initializeFab()

        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    shoppingListViewModel.shoppingListUiState.collect {
                        when (it) {
                            ShoppingListViewModel.ShoppingListUiState.Empty -> Unit
                            ShoppingListViewModel.ShoppingListUiState.Loading -> Unit
                            ShoppingListViewModel.ShoppingListUiState.Error -> Unit
                            ShoppingListViewModel.ShoppingListUiState.Success -> Unit
                            is ShoppingListViewModel.ShoppingListUiState.Updated -> {
                                updateTitle()

                                (view.adapter as ShoppingListItemRecyclerViewAdapter).submitList(
                                    it.shoppingList
                                )
                            }
                        }
                    }
                }
            }

            with(view) {
                LinearLayoutManager(context)
                adapter = ShoppingListItemRecyclerViewAdapter(
                    object :
                        ShoppingListItemRecyclerViewAdapter.ShoppingListItemListener {
                        override fun onClick(item: ShoppingListItemViewModel) {}
                        override fun onProductStatusChange(
                            item: ShoppingListItemViewModel,
                            inStock: Boolean
                        ) {
                            shoppingListViewModel.updateProductStatus(item, inStock)
                        }

                        override fun onCleared(item: ShoppingListItemViewModel) {
                            when (item.lineItemType) {
                                ShoppingListItemType.AISLE -> shoppingListViewModel.updateAisleRanks(
                                    item
                                )

                                ShoppingListItemType.PRODUCT -> shoppingListViewModel.updateProductRank(
                                    item
                                )
                            }
                        }

                        override fun onLongClick(item: ShoppingListItemViewModel): Boolean {
                            actionModeItem = item
                            return when (actionMode) {
                                null -> {
                                    // Start the CAB using the ActionMode.Callback defined earlier.
                                    actionMode =
                                        requireActivity().startActionMode(this@ShoppingListFragment)
                                    true
                                }

                                else -> false
                            }

                        }

                        override fun onMoved(item: ShoppingListItemViewModel) {
                            actionMode?.finish()
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

    private fun initializeFab() {
        val fabHandler = this.activity?.let { FabHandler(it) }

        if (fabHandler != null) {
            fabHandler.setModeShowAllFab()
            fabHandler.setFabOnClickListener(FabHandler.FabOption.ADD_PRODUCT) {
                navigateToAddProduct(shoppingListViewModel.defaultFilter)
            }
            fabHandler.setFabOnClickListener(FabHandler.FabOption.ADD_AISLE) {
                showAisleDialog(requireView().context)
            }
        }
    }

    private fun updateTitle() {
        (activity as AppCompatActivity).supportActionBar?.title =
            when (shoppingListViewModel.locationType) {
                LocationType.HOME ->
                    when (shoppingListViewModel.defaultFilter) {
                        FilterType.IN_STOCK -> resources.getString(R.string.menu_in_stock)
                        FilterType.NEEDED -> resources.getString(R.string.menu_shopping_list)
                        FilterType.ALL -> resources.getString(R.string.menu_all_items)
                    }

                LocationType.SHOP -> shoppingListViewModel.locationName
            }
    }

    private fun navigateToAddProduct(filterType: FilterType) {
        val bundle = Bundle()
        bundle.putSerializable(ARG_FILTER_TYPE, filterType)
        this.findNavController().navigate(R.id.nav_add_product, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
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
            }

            //NOTE: If you override onMenuItemSelected, OnSupportNavigateUp will only be called when returning false
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showAisleDialog(context: Context, aisle: ShoppingListItemViewModel? = null) {
        val inflater = requireActivity().layoutInflater
        val aisleDialogView = inflater.inflate(R.layout.dialog_aisle, null)
        val txtAisleName = aisleDialogView.findViewById<TextInputEditText>(R.id.edt_aisle_name)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder
            .setView(aisleDialogView)
            .setNeutralButton(R.string.cancel, null)

        if (aisle == null) {
            //Add a new Aisle
            builder
                .setTitle(R.string.add_aisle)
                .setNegativeButton(R.string.add_another) { _, _ ->
                    addNewAisle(txtAisleName.text.toString())
                    showAisleDialog(context)
                }
                .setPositiveButton(R.string.done) { _, _ ->
                    addNewAisle(txtAisleName.text.toString())
                }
        } else {
            //Edit an Aisle
            txtAisleName.setText(aisle.name)
            builder
                .setTitle(R.string.edit_aisle)
                .setPositiveButton(R.string.done) { _, _ ->
                    updateAisle(aisle.copy(name = txtAisleName.text.toString()))
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

    private fun updateAisle(aisle: ShoppingListItemViewModel) {
        if (aisle.name.isNotBlank()) {
            shoppingListViewModel.updateAisle(aisle)
        }
    }

    private fun addNewAisle(aisleName: String) {
        if (aisleName.isNotBlank()) {
            shoppingListViewModel.addAisle(aisleName)
        }
    }

    private fun confirmDelete(context: Context, item: ShoppingListItemViewModel) {
        if ((item.lineItemType == ShoppingListItemType.AISLE) && (item.inStock)) {
            Snackbar.make(
                requireView(),
                R.string.cannot_delete_default_aisle,
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder
            .setTitle(getString(R.string.delete_confirmation, item.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { _, _ ->
                shoppingListViewModel.removeItem(item)
            }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }

    private fun editShoppingListItem(item: ShoppingListItemViewModel) {


        when (item.lineItemType) {
            ShoppingListItemType.AISLE -> showAisleDialog(requireContext(), item)
            ShoppingListItemType.PRODUCT -> Toast.makeText(
                requireContext(),
                "Editing ${item.name}...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        shoppingListViewModel.submitProductSearchResults(query = newText ?: "")
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
        menu.findItem(R.id.mnu_delete_shopping_list_item).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return false // Return false if nothing is done
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_edit_shopping_list_item -> {
                actionModeItem?.let { editShoppingListItem(it) }
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

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
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