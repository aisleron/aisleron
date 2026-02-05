/*
 * Copyright (C) 2025-2026 aisleron.com
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
import com.aisleron.domain.aisle.usecase.ExpandCollapseAislesForLocationUseCase
import com.aisleron.domain.aisle.usecase.GetAislesForLocationUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.SortLocationByNameUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.shoppinglist.ShoppingListFilter
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import com.aisleron.ui.bundles.AisleListEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.listOf
import kotlin.coroutines.cancellation.CancellationException

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ShoppingListViewModel(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val sortLocationByNameUseCase: SortLocationByNameUseCase,
    private val getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase,
    private val expandCollapseAislesForLocationUseCase: ExpandCollapseAislesForLocationUseCase,
    private val getAislesForLocationUseCase: GetAislesForLocationUseCase,
    private val shoppingListItemViewModelFactory: ShoppingListItemViewModelFactory,
    debounceTime: Long = 300,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private var hydrated = false
    private var _showEmptyAisles: Boolean = true
    val productFilter: FilterType
        get() = _shoppingListFilters.value?.productFilter ?: FilterType.NEEDED

    private val _aislesForLocation = MutableStateFlow<List<AisleListEntry>>(emptyList())
    val aislesForLocation: StateFlow<List<AisleListEntry>> = _aislesForLocation

    private val _events = MutableSharedFlow<ShoppingListEvent>()
    val events = _events.asSharedFlow()

    private val _locationId = MutableStateFlow<Int?>(null)
    val locationId: StateFlow<Int?> get() = _locationId

    private val _searchQuery = MutableStateFlow("")
    private val _shoppingListFilters = MutableStateFlow<ShoppingListFilter?>(null)

    private data class SelectedSignature(
        val itemType: ShoppingListItem.ItemType,
        val itemId: Int,
        val aisleId: Int
    )

    private val _selectedSignatures = MutableStateFlow<Set<SelectedSignature>>(emptySet())

    private data class ShoppingListStateParams(
        val id: Int?,
        val query: String,
        val filters: ShoppingListFilter?,
        val selections: Set<SelectedSignature>
    )

    val shoppingListUiState: StateFlow<ShoppingListUiState> = combine(
        _locationId,
        _searchQuery.debounce(debounceTime).distinctUntilChanged(),
        _shoppingListFilters,
        _selectedSignatures
    ) { id, query, filters, selections ->
        ShoppingListStateParams(id, query, filters, selections)
    }.flatMapLatest { pkg ->
        val (id, query, filters, selections) = pkg

        if (id == null || filters == null) {
            flowOf(ShoppingListUiState.Empty)
        } else {
            val combinedFilter = filters.copy(productNameQuery = query.trim())

            getShoppingListUseCase(id, combinedFilter)
                .map { collectedLocation ->
                    val listItems = mapShoppingList(
                        collectedLocation, combinedFilter.productNameQuery.isNotBlank(), selections
                    )

                    val state: ShoppingListUiState = ShoppingListUiState.Updated(
                        shoppingList = listItems,
                        title = getListTitle(collectedLocation),
                        locationType = collectedLocation?.type ?: LocationType.HOME,
                        productFilter = combinedFilter.productFilter ?: FilterType.NEEDED
                    )

                    state
                }
                .onStart { emit(ShoppingListUiState.Loading) }
                .catch { e ->
                    emit(mapToErrorState(e))
                }
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShoppingListUiState.Loading
    )

    val loyaltyCard: StateFlow<LoyaltyCard?> = _locationId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else {
                flow<LoyaltyCard?> {
                    emit(getLoyaltyCardForLocationUseCase(id))
                }
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val selectedListItems: List<ShoppingListItem>
        get() = (shoppingListUiState.value as? ShoppingListUiState.Updated)
            ?.shoppingList?.filter { it.selected } ?: emptyList()

    private data class QtyUpdate(val item: ProductShoppingListItem, val quantity: Double?)

    private val _quantityUpdates = MutableSharedFlow<QtyUpdate>(extraBufferCapacity = 1)
    private val quantityUpdaterJob = _quantityUpdates
        .debounce(debounceTime)
        .distinctUntilChanged()
        .onEach { update ->
            coroutineScope.launchHandling {
                (update.item as ProductShoppingListItemViewModel)
                    .updateQtyNeeded(update.quantity)
            }
        }
        .launchIn(coroutineScope)

    fun toggleItemSelection(item: ShoppingListItem) {
        val signature = SelectedSignature(item.itemType, item.id, item.aisleId)
        _selectedSignatures.update { current ->
            if (current.contains(signature)) current - signature else current + signature
        }
    }

    fun clearSelectedListItems() {
        _selectedSignatures.value = emptySet()
    }

    fun hasSelectedItems(): Boolean = selectedListItems.isNotEmpty()

    private fun mapShoppingList(
        location: Location?,
        showAllProducts: Boolean,
        selections: Set<SelectedSignature>
    ): List<ShoppingListItem> {
        val filteredList: MutableList<ShoppingListItem> = location?.let { l ->
            l.aisles.flatMap { a ->
                val aisleSignature = SelectedSignature(
                    ShoppingListItem.ItemType.HEADER, a.id, a.id
                )

                listOf(
                    shoppingListItemViewModelFactory.createAisleItemViewModel(
                        a, selections.contains(aisleSignature)
                    )
                ) + a.products.filter { (a.expanded || showAllProducts) }
                    .map { ap ->
                        val productSignature = SelectedSignature(
                            ShoppingListItem.ItemType.PRODUCT, ap.product.id, ap.aisleId
                        )

                        shoppingListItemViewModelFactory.createProductItemViewModel(
                            ap, a.rank, selections.contains(productSignature)
                        )
                    }
            }
        }?.toMutableList() ?: mutableListOf()

        if (filteredList.isEmpty()) {
            filteredList.add(EmptyShoppingListItem())
        }

        return filteredList.toList()
    }

    fun hydrate(locationId: Int, productFilter: FilterType, showEmptyAisles: Boolean = false) {
        if (hydrated) return

        _showEmptyAisles = showEmptyAisles

        _locationId.value = locationId
        _shoppingListFilters.value = ShoppingListFilter(
            productFilter = productFilter,
            showEmptyAisles = _showEmptyAisles
        )

        hydrated = true
    }

    fun updateProductStatus(item: ProductShoppingListItem, inStock: Boolean) {
        coroutineScope.launchHandling {
            (item as ProductShoppingListItemViewModel).updateStatus(inStock)
        }
    }

    fun updateProductNeededQuantity(item: ProductShoppingListItem, quantity: Double?) {
        _quantityUpdates.tryEmit(QtyUpdate(item, quantity))
    }

    fun updateExpanded(item: HeaderShoppingListItem, expanded: Boolean) {
        coroutineScope.launchHandling {
            (item as HeaderShoppingListItemViewModel).updateExpanded(expanded)
        }
    }

    fun updateItemRank(item: ShoppingListItem, precedingItem: ShoppingListItem?) {
        coroutineScope.launchHandling {
            (item as ShoppingListItemViewModel).updateRank(precedingItem)
            clearSelectedListItems()
        }
    }

    fun expandCollapseAisles() {
        coroutineScope.launchHandling {
            expandCollapseAislesForLocationUseCase(_locationId.value ?: 0)
        }
    }

    fun updateSelectedProductAisle(selectedAisleId: Int) {
        val items = selectedListItems.filterIsInstance<ProductShoppingListItemViewModel>()
        coroutineScope.launchHandling {
            items.forEach {
                it.updateAisle(selectedAisleId)
            }

            clearSelectedListItems()
        }
    }

    fun submitProductSearch(productNameQuery: String) {
        if (productNameQuery.isBlank()) return

        _searchQuery.value = productNameQuery.trim()
    }

    fun setShowEmptyAisles(showEmptyAisles: Boolean) {
        if (_showEmptyAisles == showEmptyAisles) return

        _showEmptyAisles = showEmptyAisles
        _shoppingListFilters.update { it?.copy(showEmptyAisles = _showEmptyAisles) }
    }

    fun requestListRefresh(returnDefaultList: Boolean) {
        if (!hydrated || !returnDefaultList) return

        clearSelectedListItems()
        _searchQuery.value = ""
        _shoppingListFilters.update {
            it?.copy(
                productNameQuery = "",
                showEmptyAisles = _showEmptyAisles
            )
        }
    }

    fun movedItem(item: ShoppingListItem) {
        //TODO: Do some smarts to only expand the list if I'm dragging an aisle,
        //      dragging a product across an aisle, or reached the end of the list
        //TODO: When dragging an aisle, hide all products
        if (_shoppingListFilters.value?.showEmptyAisles ?: _showEmptyAisles) return

        _shoppingListFilters.update { it?.copy(showEmptyAisles = true) }
    }

    fun removeSelectedItems() {
        val aisleItems = selectedListItems.filterIsInstance<AisleShoppingListItemViewModel>()
        val productItems = selectedListItems.filterIsInstance<ProductShoppingListItemViewModel>()
        coroutineScope.launchHandling {
            productItems.forEach { it.remove() }
            aisleItems.forEach { it.remove() }
            clearSelectedListItems()
        }
    }

    fun sortListByName() {
        coroutineScope.launchHandling {
            sortLocationByNameUseCase(_locationId.value ?: 0)
        }
    }

    fun requestLocationAisles() {
        coroutineScope.launchHandling {
            val aisles = getAislesForLocationUseCase(_locationId.value ?: 0)
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
        } catch (e: Exception) {
            val event = mapToErrorEvent(e)
            _events.emit(event)
        }
    }

    private fun getErrorInfo(e: Throwable): Pair<AisleronException.ExceptionCode, String?> {
        return if (e is AisleronException) {
            e.exceptionCode to e.message
        } else {
            AisleronException.ExceptionCode.GENERIC_EXCEPTION to e.message
        }
    }

    private fun mapToErrorState(e: Throwable): ShoppingListUiState.Error {
        val (code, message) = getErrorInfo(e)
        return ShoppingListUiState.Error(code, message)
    }

    private fun mapToErrorEvent(e: Throwable): ShoppingListEvent.ShowError {
        val (code, message) = getErrorInfo(e)
        return ShoppingListEvent.ShowError(code, message)
    }

    fun getSelectedItemAisleId(): Int =
        selectedListItems.singleOrNull()?.aisleId ?: -1

    fun navigateToLoyaltyCard() {
        loyaltyCard.value ?: return

        coroutineScope.launchHandling {
            _events.emit(ShoppingListEvent.NavigateToLoyaltyCard(loyaltyCard.value))
        }
    }

    fun navigateToEditShop() {
        _locationId.value ?: return

        coroutineScope.launchHandling {
            _events.emit(ShoppingListEvent.NavigateToEditShop(_locationId.value))
        }
    }

    private fun getListTitle(collectedLocation: Location?): ListTitle =
        when (productFilter) {
            FilterType.IN_STOCK -> ListTitle.InStock
            FilterType.NEEDED -> ListTitle.Needed
            FilterType.ALL -> ListTitle.AllItems
        }.let { baseTitle ->
            if (collectedLocation == null || collectedLocation.type == LocationType.HOME) {
                baseTitle
            } else {
                ListTitle.LocationName(collectedLocation.name)
            }
        }

    sealed class ShoppingListUiState {
        data object Empty : ShoppingListUiState()
        data object Loading : ShoppingListUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ShoppingListUiState()

        data class Updated(
            val shoppingList: List<ShoppingListItem>,
            val title: ListTitle,
            val locationType: LocationType,
            val productFilter: FilterType
        ) : ShoppingListUiState()
    }

    sealed class ShoppingListEvent {
        data class ShowError(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ShoppingListEvent()

        data class NavigateToLoyaltyCard(val loyaltyCard: LoyaltyCard?) : ShoppingListEvent()
        data class NavigateToEditShop(val id: Int?) : ShoppingListEvent()
    }

    sealed class ListTitle {
        data class LocationName(val name: String) : ListTitle()
        object InStock : ListTitle()
        object Needed : ListTitle()
        object AllItems : ListTitle()
        object AllShops : ListTitle()
    }

    companion object {
        const val TAG = "ShoppingListViewModel"
    }
}


