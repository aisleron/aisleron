package com.aisleron.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.location.Location
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

    private var location: Location? = null

    val locationName: String get() = location?.name.toString()
    val locationType: LocationType get() = location?.type ?: LocationType.HOME

    var filterType: FilterType = FilterType.NEEDED
        private set

    private val _shoppingListUiState = MutableStateFlow<ShoppingListUiState>(
        ShoppingListUiState.Empty
    )
    val shoppingListUiState = _shoppingListUiState.asStateFlow()

    fun hydrate(locationId: Int, filterType: FilterType) {
        this.filterType = filterType
        refreshListItems(locationId)
    }

    private fun refreshListItems(locationId: Int) {
        viewModelScope.launch {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            getShoppingListUseCase(locationId, filterType).collect { loc ->
                location = loc

                val shoppingList = mutableListOf<ShoppingListItemViewModel>()

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
                                (p.product.inStock && filterType == FilterType.IN_STOCK)
                                        || (!p.product.inStock && filterType == FilterType.NEEDED)
                                        || (filterType == FilterType.ALL)
                            }
                        )
                    )
                    shoppingList += a.products.filter { p ->
                        (p.product.inStock && filterType == FilterType.IN_STOCK)
                                || (!p.product.inStock && filterType == FilterType.NEEDED)
                                || (filterType == FilterType.ALL)
                    }.map { p ->
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
                _shoppingListUiState.value = ShoppingListUiState.Updated(shoppingList.toList())
            }
        }
    }

    fun updateProductStatus(item: ShoppingListItemViewModel, inStock: Boolean) {
        viewModelScope.launch {
            updateProductStatusUseCase(item.id, inStock)
        }
    }

    fun addAisle(aisleName: String) {
        if (location != null) {
            viewModelScope.launch {
                addAisleUseCase(
                    Aisle(
                        name = aisleName,
                        products = emptyList(),
                        locationId = location!!.id,
                        isDefault = false,
                        rank = 0,
                        id = 0
                    )
                )
            }
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
                        locationId = location!!.id,
                        isDefault = it.inStock,
                    )
                })
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