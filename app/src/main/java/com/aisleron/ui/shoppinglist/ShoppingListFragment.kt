package com.aisleron.ui.shoppinglist

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
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
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * A fragment representing a list of [ShoppingListItemViewModel].
 */
class ShoppingListFragment : Fragment(), SearchView.OnQueryTextListener {

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
                        override fun onAisleClick(item: ShoppingListItemViewModel) {}
                        override fun onProductClick(item: ShoppingListItemViewModel) {}
                        override fun onProductStatusChange(
                            item: ShoppingListItemViewModel,
                            inStock: Boolean
                        ) {
                            shoppingListViewModel.updateProductStatus(item, inStock)
                        }

                        override fun onProductMoved(product: ShoppingListItemViewModel) {
                            shoppingListViewModel.updateProductRank(product)
                        }

                        override fun onAisleMoved(aisle: ShoppingListItemViewModel) {
                            shoppingListViewModel.updateAisleRanks(aisle)
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
                showAddAisleDialog(requireView().context)
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
                searchView.setSearchableInfo(searchableInfo)
                searchView.setOnQueryTextListener(this@ShoppingListFragment)
                searchView.setOnCloseListener {
                    shoppingListViewModel.requestDefaultList()
                    false
                }

                val searchCloseButtonId =
                    searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn).id
                val closeButton = searchView.findViewById<ImageView>(searchCloseButtonId)
                closeButton?.setOnClickListener {
                    searchView.onActionViewCollapsed()
                    shoppingListViewModel.requestDefaultList()
                }
            }

            //NOTE: If you override onMenuItemSelected, OnSupportNavigateUp will only be called when returning false
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showAddAisleDialog(context: Context) {
        val inflater = requireActivity().layoutInflater
        val aisleDialogView = inflater.inflate(R.layout.dialog_aisle, null)
        val txtAisleName = aisleDialogView.findViewById<TextInputEditText>(R.id.edt_aisle_name)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder
            .setTitle(R.string.add_aisle)
            .setView(aisleDialogView)
            .setNegativeButton(R.string.add_another) { _, _ ->
                addNewAisle(txtAisleName.text.toString())
                showAddAisleDialog(context)
            }
            .setPositiveButton(R.string.done) { _, _ ->
                addNewAisle(txtAisleName.text.toString())
            }
            .setNeutralButton(R.string.cancel, null)

        val dialog: AlertDialog = builder.create()

        txtAisleName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }

        txtAisleName.requestFocus()
        dialog.show()
    }

    private fun addNewAisle(aisleName: String) {
        if (aisleName.isNotBlank()) {
            shoppingListViewModel.addAisle(aisleName)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        shoppingListViewModel.submitProductSearchResults(query = newText ?: "")
        return false
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