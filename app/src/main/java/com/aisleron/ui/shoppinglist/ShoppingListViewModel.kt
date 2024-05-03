package com.aisleron.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val updateProductStatusUseCase: UpdateProductStatusUseCase,
    private val addAisleUseCase: AddAisleUseCase,
    private val updateAisleProductsUseCase: UpdateAisleProductsUseCase,
    private val updateAislesUseCase: UpdateAislesUseCase
) : ViewModel() {

    //private var location: Location? = null

    private var _locationName: String = ""
    val locationName: String get() = _locationName

    private var _locationType: LocationType = LocationType.HOME
    val locationType: LocationType get() = _locationType

    private var _defaultFilter: FilterType = FilterType.NEEDED
    val defaultFilter: FilterType get() = _defaultFilter

    private val shoppingList = mutableListOf<ShoppingListItemViewModel>()

    private var _locationId: Int = 0

    private var _listFilter: (ShoppingListItemViewModel) -> Boolean = { _ -> false }

    private val _shoppingListUiState = MutableStateFlow<ShoppingListUiState>(
        ShoppingListUiState.Empty
    )
    val shoppingListUiState = _shoppingListUiState.asStateFlow()

    fun hydrate(locationId: Int, filterType: FilterType) {
        _defaultFilter = filterType
        _listFilter = getListFilterByProductFilter(_defaultFilter)
        refreshListItems(locationId)
    }


    private fun getListFilterByProductFilter(filter: FilterType): (ShoppingListItemViewModel) -> Boolean {
        return { sli: ShoppingListItemViewModel ->
            (sli.lineItemType == ShoppingListItemType.AISLE) || (
                    (sli.lineItemType == ShoppingListItemType.PRODUCT) && (
                            (sli.inStock && filter == FilterType.IN_STOCK) ||
                                    (!sli.inStock && filter == FilterType.NEEDED) ||
                                    (filter == FilterType.ALL)
                            )
                    )}
    }

    private fun refreshListItems(locationId: Int) {
        viewModelScope.launch {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            getShoppingListUseCase(locationId).collect { location ->
                location?.let {
                    _locationName = it.name
                    _locationType = it.type
                    _locationId = it.id
                }

                shoppingList.clear()

                location?.aisles?.forEach { a ->
                    shoppingList.add(
                        ShoppingListItemViewModel(
                            lineItemType = ShoppingListItemType.AISLE,
                            aisleRank = a.rank,
                            rank = a.rank,
                            id = a.id,
                            name = a.name,
                            inStock = a.isDefault,  //inStock holds the aisle default value in the shopping list
                            aisleId = a.id,
                            mappingId = 0,
                            childCount = a.products.count { p ->
                                (p.product.inStock && defaultFilter == FilterType.IN_STOCK)
                                        || (!p.product.inStock && defaultFilter == FilterType.NEEDED)
                                        || (defaultFilter == FilterType.ALL)
                            }
                        )
                    )
                    shoppingList += a.products.map { p ->
                        ShoppingListItemViewModel(
                            lineItemType = ShoppingListItemType.PRODUCT,
                            aisleRank = a.rank,
                            rank = p.rank,
                            id = p.product.id,
                            name = p.product.name,
                            inStock = p.product.inStock,
                            aisleId = p.aisleId,
                            mappingId = p.id,
                            childCount = 0
                        )
                    }
                }

                shoppingList.sortWith(
                    compareBy(
                        { it.aisleRank },
                        { it.aisleId },
                        { it.lineItemType },
                        { it.rank },
                        { it.name })
                )
                _shoppingListUiState.value =
                    ShoppingListUiState.Updated(shoppingList.filter(_listFilter))
            }
        }
    }


    fun updateProductStatus(item: ShoppingListItemViewModel, inStock: Boolean) {
        viewModelScope.launch {
            updateProductStatusUseCase(item.id, inStock)
        }
    }

    fun addAisle(aisleName: String) {
        viewModelScope.launch {
            addAisleUseCase(
                Aisle(
                    name = aisleName,
                    products = emptyList(),
                    locationId = _locationId,
                    isDefault = false,
                    rank = 0,
                    id = 0
                )
            )
        }
    }

    fun updateProductRanks(shoppingList: List<ShoppingListItemViewModel>) {
        viewModelScope.launch {
            updateAisleProductsUseCase(shoppingList.filter { it.lineItemType == ShoppingListItemType.PRODUCT && it.modified }
                .map {
                    AisleProduct(
                        rank = it.rank,
                        aisleId = it.aisleId,
                        product = Product(id = it.id, name = it.name, inStock = it.inStock),
                        id = it.mappingId
                    )
                })
        }
    }

    fun updateAisleRanks(shoppingList: List<ShoppingListItemViewModel>) {
        viewModelScope.launch {
            updateAislesUseCase(shoppingList.filter { it.lineItemType == ShoppingListItemType.AISLE && it.modified }
                .map {
                    Aisle(
                        rank = it.rank,
                        id = it.id,
                        name = it.name,
                        products = emptyList(),
                        locationId = _locationId,
                        isDefault = it.inStock,
                    )
                })
        }
    }

    fun submitProductSearchResults(query: String) {
        _listFilter = { sli ->
            (sli.lineItemType == ShoppingListItemType.AISLE) || (
                    (sli.lineItemType == ShoppingListItemType.PRODUCT) && (sli.name.contains(query, true))
                    )
        }
        viewModelScope.launch {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            val searchResults = shoppingList.filter(_listFilter)
            _shoppingListUiState.value = ShoppingListUiState.Updated(searchResults)
        }
    }

    fun requestDefaultList() {
        _listFilter = getListFilterByProductFilter(_defaultFilter)
        viewModelScope.launch {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            _shoppingListUiState.value =
                ShoppingListUiState.Updated(shoppingList.filter(_listFilter))
        }
    }

    sealed class ShoppingListUiState {
        data object Empty : ShoppingListUiState()
        data object Loading : ShoppingListUiState()
        data object Error : ShoppingListUiState()
        data object Success : ShoppingListUiState()
        data class Updated(val shoppingList: List<ShoppingListItemViewModel>) :
            ShoppingListUiState()
    }
}