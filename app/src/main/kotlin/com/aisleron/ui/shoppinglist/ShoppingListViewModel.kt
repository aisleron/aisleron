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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.ExpandCollapseAislesForLocationUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAislesForLocationUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.SortLocationByNameUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import com.aisleron.ui.bundles.AisleListEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.listOf
import kotlin.coroutines.cancellation.CancellationException

class ShoppingListViewModel(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val updateProductStatusUseCase: UpdateProductStatusUseCase,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val updateAisleRankUseCase: UpdateAisleRankUseCase,
    private val removeAisleUseCase: RemoveAisleUseCase,
    private val removeProductUseCase: RemoveProductUseCase,
    private val getAisleUseCase: GetAisleUseCase,
    private val updateAisleExpandedUseCase: UpdateAisleExpandedUseCase,
    private val sortLocationByNameUseCase: SortLocationByNameUseCase,
    private val getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase,
    private val updateProductQtyNeededUseCase: UpdateProductQtyNeededUseCase,
    private val expandCollapseAislesForLocationUseCase: ExpandCollapseAislesForLocationUseCase,
    private val getAislesForLocationUseCase: GetAislesForLocationUseCase,
    private val changeProductAisleUseCase: ChangeProductAisleUseCase,
    private val debounceTime: Long = 300,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private var searchJob: Job? = null
    private var updateQtyJob: Job? = null

    private var hydrated = false

    private var _showDefaultAisle: Boolean = true
    private val showDefaultAisle: Boolean get() = _showDefaultAisle

    private var _location: Location? = null
    val locationName: String get() = _location?.name ?: ""
    val locationType: LocationType get() = _location?.type ?: LocationType.HOME
    val locationId: Int get() = _location?.id ?: 0

    private var _defaultFilter: FilterType = FilterType.NEEDED
    val defaultFilter: FilterType get() = _defaultFilter

    private var _showEmptyAisles: Boolean = true
    private val showEmptyAisles: Boolean get() = _showEmptyAisles

    private var _loyaltyCard: LoyaltyCard? = null
    val loyaltyCard: LoyaltyCard? get() = _loyaltyCard

    private lateinit var shoppingListFilterParameters: ShoppingListFilterParameters

    private val _shoppingListUiState = MutableStateFlow<ShoppingListUiState>(
        ShoppingListUiState.Empty
    )

    val shoppingListUiState = _shoppingListUiState.asStateFlow()

    private val _aislesForLocation = MutableStateFlow<List<AisleListEntry>>(emptyList())
    val aislesForLocation: StateFlow<List<AisleListEntry>> = _aislesForLocation

    private var _listItems = listOf<ShoppingListItem>()

    val selectedListItems: List<ShoppingListItem> get() = _listItems.filter { it.selected }

    private fun getListWithItemSelectionToggled(
        sourceListUiState: List<ShoppingListItem>, item: ShoppingListItem
    ): List<ShoppingListItem> {
        val updatedList = sourceListUiState.map { listItem ->
            if (listItem == item) {
                listItem.copyWith(selected = !listItem.selected)
            } else {
                listItem
            }
        }

        return updatedList
    }

    fun toggleItemSelection(item: ShoppingListItem) {
        coroutineScope.launchHandling {
            val updatedList = getListWithItemSelectionToggled(_listItems, item)
            submitUpdatedList(updatedList)
        }
    }

    private fun clearSelectedListItemsInternal() {
        if (!hasSelectedItems()) return

        val deselectedList = _listItems.map { listItem ->
            listItem.copyWith(selected = false)
        }

        submitUpdatedList(deselectedList)
    }

    fun clearSelectedListItems() {
        coroutineScope.launchHandling { clearSelectedListItemsInternal() }
    }

    fun hasSelectedItems(): Boolean = selectedListItems.isNotEmpty()

    private fun submitUpdatedList(list: List<ShoppingListItem>) {
        _listItems = list
        _shoppingListUiState.value = ShoppingListUiState.Updated(_listItems)
    }

    private fun isValidAisle(aisle: Aisle, parameters: ShoppingListFilterParameters): Boolean {
        return (parameters.showDefaultAisle || !aisle.isDefault) &&
                (aisle.products.count { isValidAisleProduct(it, parameters) } > 0 ||
                        parameters.showAllAisles)
    }

    private fun isValidAisleProduct(
        ap: AisleProduct, parameters: ShoppingListFilterParameters, aisleExpanded: Boolean = true
    ): Boolean {
        return ((ap.product.inStock && parameters.filterType == FilterType.IN_STOCK) ||
                (!ap.product.inStock && parameters.filterType == FilterType.NEEDED) ||
                (parameters.filterType == FilterType.ALL)
                ) &&
                (parameters.productNameQuery == "" || (ap.product.name.contains(
                    parameters.productNameQuery.trim(), true
                ))) &&
                ((parameters.showAllProducts || aisleExpanded))
    }

    private fun getShoppingList(
        location: Location?, parameters: ShoppingListFilterParameters
    ): List<ShoppingListItem> {
        val filteredList: MutableList<ShoppingListItem> = location?.let { l ->
            l.aisles.filter { a -> isValidAisle(a, parameters) }.flatMap { a ->
                listOf(
                    AisleShoppingListItemViewModel(
                        rank = a.rank,
                        id = a.id,
                        name = a.name,
                        isDefault = a.isDefault,
                        locationId = a.locationId,
                        expanded = a.expanded,
                        selected = false,
                        updateAisleRankUseCase = updateAisleRankUseCase,
                        getAisleUseCase = getAisleUseCase,
                        removeAisleUseCase = removeAisleUseCase,
                        childCount = a.products.count { ap -> isValidAisleProduct(ap, parameters) }
                    )) +
                        a.products.filter { ap -> isValidAisleProduct(ap, parameters, a.expanded) }
                            .map { ap ->
                                ProductShoppingListItemViewModel(
                                    aisleRank = a.rank,
                                    rank = ap.rank,
                                    id = ap.product.id,
                                    name = ap.product.name,
                                    inStock = ap.product.inStock,
                                    qtyNeeded = ap.product.qtyNeeded,
                                    noteId = ap.product.noteId,
                                    aisleId = ap.aisleId,
                                    selected = false,
                                    aisleProductId = ap.id,
                                    removeProductUseCase = removeProductUseCase,
                                    updateAisleProductRankUseCase = updateAisleProductRankUseCase,
                                    qtyIncrement = ap.product.qtyIncrement,
                                    unitOfMeasure = ap.product.unitOfMeasure,
                                    trackingMode = ap.product.trackingMode
                                )
                            }
            }
        }?.toMutableList() ?: mutableListOf()

        filteredList.sortWith(
            compareBy(
                { it.aisleRank },
                { it.aisleId },
                { it.itemType },
                { it.rank },
                { it.name })
        )

        if (filteredList.isEmpty()) {
            filteredList.add(EmptyShoppingListItem())
        }

        return filteredList.toList()
    }

    private fun getDefaultFilterParameters(): ShoppingListFilterParameters {
        return ShoppingListFilterParameters(
            filterType = _defaultFilter,
            showDefaultAisle = showDefaultAisle,
            showAllAisles = showEmptyAisles
        )
    }

    fun hydrate(locationId: Int, filterType: FilterType, showEmptyAisles: Boolean = false) {
        if (hydrated) return

        hydrated = true

        _defaultFilter = filterType
        _showEmptyAisles = showEmptyAisles
        coroutineScope.launchHandling {
            getShoppingListUseCase(locationId).collect { collectedLocation ->
                _shoppingListUiState.value = ShoppingListUiState.Loading
                _location = collectedLocation

                if (!::shoppingListFilterParameters.isInitialized)
                    shoppingListFilterParameters = getDefaultFilterParameters()

                shoppingListFilterParameters.showAllAisles = _showEmptyAisles
                _location?.let {
                    // If default aisle preference has changed, update the filter parameters accordingly
                    if (it.showDefaultAisle != _showDefaultAisle) {
                        _showDefaultAisle = it.showDefaultAisle
                        shoppingListFilterParameters.showDefaultAisle = _showDefaultAisle
                    }

                    _loyaltyCard = getLoyaltyCardForLocationUseCase(it.id)
                }

                submitUpdatedList(getShoppingList(_location, shoppingListFilterParameters))
            }
        }
    }

    fun updateProductStatus(item: ProductShoppingListItem, inStock: Boolean) {
        coroutineScope.launchHandling {
            updateProductStatusUseCase(item.id, inStock)
        }
    }

    fun updateProductNeededQuantity(item: ProductShoppingListItem, quantity: Double?) {
        updateQtyJob?.cancel()
        updateQtyJob = coroutineScope.launchHandling {
            delay(debounceTime)
            quantity?.let { updateProductQtyNeededUseCase(item.id, it) }
        }
    }

    fun updateAisleExpanded(item: AisleShoppingListItem, expanded: Boolean) {
        coroutineScope.launchHandling {
            updateAisleExpandedUseCase(item.id, expanded)
        }
    }

    fun updateItemRank(item: ShoppingListItem, precedingItem: ShoppingListItem?) {
        coroutineScope.launchHandling {
            (item as ShoppingListItemViewModel).updateRank(precedingItem)
        }
    }

    fun expandCollapseAisles() {
        coroutineScope.launchHandling {
            expandCollapseAislesForLocationUseCase(locationId)
        }
    }

    fun updateSelectedProductAisle(selectedAisleId: Int) {
        val items = selectedListItems.filterIsInstance<ProductShoppingListItem>()
        coroutineScope.launchHandling {
            items.forEach {
                changeProductAisleUseCase(it.id, it.aisleId, selectedAisleId)
                clearSelectedListItemsInternal()
            }
        }
    }

    fun submitProductSearch(productNameQuery: String) {
        searchJob?.cancel()

        searchJob = coroutineScope.launchHandling {
            delay(debounceTime) //Add debounce to the search so it doesn't execute every keypress

            shoppingListFilterParameters.filterType = FilterType.ALL
            shoppingListFilterParameters.showDefaultAisle = true
            shoppingListFilterParameters.productNameQuery = productNameQuery
            shoppingListFilterParameters.showAllProducts = true
            shoppingListFilterParameters.showAllAisles = showEmptyAisles

            _shoppingListUiState.value = ShoppingListUiState.Loading
            val searchResults = getShoppingList(_location, shoppingListFilterParameters)
            submitUpdatedList(searchResults)
        }
    }

    fun setShowEmptyAisles(showEmptyAisles: Boolean) {
        if (_showEmptyAisles != showEmptyAisles) {
            _showEmptyAisles = showEmptyAisles
            requestDefaultList()
        }
    }

    fun requestDefaultList() {
        searchJob?.cancel()
        shoppingListFilterParameters = getDefaultFilterParameters()
        coroutineScope.launchHandling {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            val searchResults = getShoppingList(_location, shoppingListFilterParameters)
            submitUpdatedList(searchResults)
        }
    }

    fun movedItem(item: ShoppingListItem) {
        //TODO: Do some smarts to only expand the list if I'm dragging an aisle, dragging a product across an aisle, or reached the end of the list
        //TODO: When dragging an aisle, hide all products
        shoppingListFilterParameters.showAllAisles = true
        coroutineScope.launchHandling {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            val shoppingList = getShoppingList(_location, shoppingListFilterParameters)
            val updatedList =
                getListWithItemSelectionToggled(shoppingList, item.copyWith(selected = false))

            submitUpdatedList(updatedList)
        }
    }

    fun removeSelectedItems() {
        val aisleItems = selectedListItems.filterIsInstance<AisleShoppingListItemViewModel>()
        val productItems = selectedListItems.filterIsInstance<ProductShoppingListItemViewModel>()
        coroutineScope.launchHandling {
            productItems.forEach { it.remove() }
            aisleItems.forEach { it.remove() }
            clearSelectedListItemsInternal()
        }
    }

    fun sortListByName() {
        coroutineScope.launchHandling {
            sortLocationByNameUseCase(locationId)
        }
    }

    fun clearState() {
        _shoppingListUiState.value = ShoppingListUiState.Empty
    }

    fun requestLocationAisles() {
        coroutineScope.launchHandling {
            val aisles = getAislesForLocationUseCase(locationId)
                .sortedBy { it.rank }
                .map { AisleListEntry(it.id, it.name) }

            _aislesForLocation.value = aisles
        }
    }

    fun clearLocationAisles() {
        _aislesForLocation.value = emptyList()
    }

    private fun CoroutineScope.launchHandling(
        block: suspend CoroutineScope.() -> Unit
    ) = launch {
        try {
            block()
        } catch (e: CancellationException) {
            Log.e(TAG, "Cancellation")
            throw e // propagate cancellation
        } catch (e: AisleronException) {
            _shoppingListUiState.value =
                ShoppingListUiState.Error(e.exceptionCode, e.message)
        } catch (e: Exception) {
            _shoppingListUiState.value = ShoppingListUiState.Error(
                AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
            )
        }
    }

    fun getSelectedItemAisleId(): Int =
        selectedListItems.singleOrNull()?.aisleId ?: -1

    sealed class ShoppingListUiState {
        data object Empty : ShoppingListUiState()
        data object Loading : ShoppingListUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ShoppingListUiState()

        data class Updated(val shoppingList: List<ShoppingListItem>) : ShoppingListUiState()
    }

    companion object {
        const val TAG = "ShoppingListViewModel"
    }
}