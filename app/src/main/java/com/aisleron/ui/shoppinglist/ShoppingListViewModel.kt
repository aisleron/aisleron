package com.aisleron.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val updateProductStatusUseCase: UpdateProductStatusUseCase,
    private val addAisleUseCase: AddAisleUseCase
) : ViewModel() {

    private var location: Location? = null

    val locationName: String get() = location?.name.toString()
    val locationType: LocationType get() = location?.type ?: LocationType.HOME

    var filterType: FilterType = FilterType.NEEDED
        private set

    private val _items = mutableListOf<ShoppingListItemViewModel>()
    val items: List<ShoppingListItemViewModel> = _items

    private val _shoppingListUiState = MutableStateFlow<ShoppingListUiState>(
        ShoppingListUiState.Empty
    )
    val shoppingListUiState: StateFlow<ShoppingListUiState> = _shoppingListUiState

    fun hydrate(locationId: Int, filterType: FilterType) {
        viewModelScope.launch {
            this@ShoppingListViewModel.filterType = filterType
            _shoppingListUiState.value = ShoppingListUiState.Loading
            refreshListItems(locationId)
            _shoppingListUiState.value = ShoppingListUiState.Success(items)
        }
    }

    fun updateProductStatus(item: ShoppingListItemViewModel) {
        viewModelScope.launch {
            item.inStock?.let {
                updateProductStatusUseCase(item.id, it)
            }
        }
    }

    private suspend fun refreshListItems(locationId: Int) {
        location = getShoppingListUseCase(locationId, filterType)

        _items.clear()

        location?.aisles?.forEach { a ->
            _items.add(
                ShoppingListItemViewModel(
                    lineItemType = ShoppingListItemType.AISLE,
                    aisleRank = a.rank,
                    productRank = -1,
                    id = a.id,
                    name = a.name,
                    inStock = null
                )
            )
            _items += a.products.filter { p ->
                (p.product.inStock && filterType == FilterType.IN_STOCK)
                        || (!p.product.inStock && filterType == FilterType.NEEDED)
                        || (filterType == FilterType.ALL)
            }.map { p ->
                ShoppingListItemViewModel(
                    lineItemType = ShoppingListItemType.PRODUCT,
                    aisleRank = a.rank,
                    productRank = p.rank,
                    id = p.product.id,
                    name = p.product.name,
                    inStock = p.product.inStock
                )
            }
        }

        _items.sortWith(compareByDescending<ShoppingListItemViewModel> { it.aisleRank }.thenBy { it.lineItemType }
            .thenByDescending { it.productRank }.thenBy { it.name })
        //TODO: Add Aisle Id and/or Aisle Object & Product Object items to view model list
    }

    fun removeItem(item: ShoppingListItemViewModel) {
        _items.remove(item)
    }

    fun addAisle(aisleName: String) {
        if (location != null) {
            viewModelScope.launch {
                _shoppingListUiState.value = ShoppingListUiState.Loading
                addAisleUseCase(
                    Aisle(
                        name = aisleName,
                        products = emptyList(),
                        locationId = location!!.id,
                        isDefault = false,
                        rank = 1,
                        id = 0
                    )
                )
                refreshListItems(location!!.id)
                _shoppingListUiState.value = ShoppingListUiState.Success(items)
            }
        }

    }

    sealed class ShoppingListUiState {
        data object Empty : ShoppingListUiState()
        data object Loading : ShoppingListUiState()

        //data object Error : ShoppingListUiState()
        data class Success(val shoppingList: List<ShoppingListItemViewModel>) :
            ShoppingListUiState()
    }
}