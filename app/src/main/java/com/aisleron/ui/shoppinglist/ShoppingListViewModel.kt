package com.aisleron.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val locationRepository: LocationRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private var location: Location? = null

    val locationName: String get() = location?.name.toString()
    val locationType: LocationType get() = location?.type ?: LocationType.HOME

    var filterType: FilterType = FilterType.NEEDED
        private set

    private val _items = mutableListOf<ShoppingListItemViewModel>()
    val items: List<ShoppingListItemViewModel> = _items

    private val _shoppingListUiState = MutableStateFlow<ShoppingListUiState>(
        ShoppingListUiState.Empty,
    )
    val shoppingListUiState: StateFlow<ShoppingListUiState> = _shoppingListUiState

    fun hydrate(locationId: Int, filterType: FilterType) {
        viewModelScope.launch {
            this@ShoppingListViewModel.filterType = filterType
            _shoppingListUiState.value = ShoppingListUiState.Loading
            location = locationRepository.getLocationWithAislesWithProducts(locationId)
            refreshListItems()
            _shoppingListUiState.value = ShoppingListUiState.Success(items)
        }
    }

    fun updateProductStatus(item: ShoppingListItemViewModel) {
        viewModelScope.launch {
            item.inStock?.let {
                productRepository.updateStatus(item.id, it)
            }
        }
    }

    fun refreshListItems() {
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

        _items.sortWith(compareBy({ it.aisleRank }, { it.productRank }))
        //TODO: Add Aisle Id and/or Aisle Object & Product Object items to view model list
    }

    fun removeItem(item: ShoppingListItemViewModel) {
        _items.remove(item)
    }

    sealed class ShoppingListUiState {
        data object Empty : ShoppingListUiState()
        data object Loading : ShoppingListUiState()

        //data object Error : ShoppingListUiState()
        data class Success(val shoppingList: List<ShoppingListItemViewModel>) :
            ShoppingListUiState()
    }
}