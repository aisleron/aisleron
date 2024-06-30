package com.aisleron.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val updateProductStatusUseCase: UpdateProductStatusUseCase,
    private val addAisleUseCase: AddAisleUseCase,
    private val updateAisleUseCase: UpdateAisleUseCase,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val updateAisleRankUseCase: UpdateAisleRankUseCase,
    private val removeAisleUseCase: RemoveAisleUseCase,
    private val removeProductUseCase: RemoveProductUseCase,
    private val getAisleUseCase: GetAisleUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private var _locationName: String = ""
    val locationName: String get() = _locationName

    private var _locationType: LocationType = LocationType.HOME
    val locationType: LocationType get() = _locationType

    private var _defaultFilter: FilterType = FilterType.NEEDED
    val defaultFilter: FilterType get() = _defaultFilter

    private val shoppingList = mutableListOf<ShoppingListItem>()
    private var _locationId: Int = 0
    private var _listFilter: (ShoppingListItem) -> Boolean =
        getListFilterByProductFilter(_defaultFilter)
    private val _shoppingListUiState = MutableStateFlow<ShoppingListUiState>(
        ShoppingListUiState.Empty
    )

    val shoppingListUiState = _shoppingListUiState.asStateFlow()

    fun hydrate(locationId: Int, filterType: FilterType) {
        _defaultFilter = filterType
        _listFilter = getListFilterByProductFilter(_defaultFilter)
        refreshListItems(locationId)
    }

    private fun getListFilterByProductFilter(filter: FilterType): (ShoppingListItem) -> Boolean {
        return { sli: ShoppingListItem ->
            (sli is AisleShoppingListItem) || (
                    (sli is ProductShoppingListItem) && (
                            (sli.inStock && filter == FilterType.IN_STOCK) ||
                                    (!sli.inStock && filter == FilterType.NEEDED) ||
                                    (filter == FilterType.ALL)
                            )
                    )
        }
    }

    private fun refreshListItems(locationId: Int) {
        coroutineScope.launch {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            getShoppingListUseCase(locationId).collect { location ->
                shoppingList.clear()
                location?.let {
                    _locationName = it.name
                    _locationType = it.type
                    _locationId = it.id

                    it.aisles.forEach { a ->
                        shoppingList.add(
                            AisleShoppingListItem(
                                rank = a.rank,
                                id = a.id,
                                name = a.name,
                                isDefault = a.isDefault,
                                updateAisleRankUseCase = updateAisleRankUseCase,
                                getAisleUseCase = getAisleUseCase,
                                removeAisleUseCase = removeAisleUseCase,
                                locationId = _locationId,
                                childCount = a.products.count { p ->
                                    (p.product.inStock && defaultFilter == FilterType.IN_STOCK)
                                            || (!p.product.inStock && defaultFilter == FilterType.NEEDED)
                                            || (defaultFilter == FilterType.ALL)
                                }
                            )
                        )
                        shoppingList += a.products.map { p ->
                            ProductShoppingListItem(
                                aisleRank = a.rank,
                                rank = p.rank,
                                id = p.product.id,
                                name = p.product.name,
                                inStock = p.product.inStock,
                                aisleId = p.aisleId,
                                aisleProductId = p.id,
                                removeProductUseCase = removeProductUseCase,
                                updateAisleProductRankUseCase = updateAisleProductRankUseCase
                            )
                        }
                    }
                }

                shoppingList.sortWith(
                    compareBy(
                        { it.aisleRank },
                        { it.aisleId },
                        { it.itemType },
                        { it.rank },
                        { it.name })
                )
                _shoppingListUiState.value =
                    ShoppingListUiState.Updated(shoppingList.filter(_listFilter))
            }
        }
    }

    fun updateProductStatus(item: ProductShoppingListItem, inStock: Boolean) {
        coroutineScope.launch {
            updateProductStatusUseCase(item.id, inStock)
        }
    }

    fun addAisle(aisleName: String) {
        coroutineScope.launch {
            try {
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

            } catch (e: AisleronException) {
                _shoppingListUiState.value = ShoppingListUiState.Error(e.exceptionCode, e.message)

            } catch (e: Exception) {
                _shoppingListUiState.value =
                    ShoppingListUiState.Error(AisleronException.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    fun updateAisle(aisle: AisleShoppingListItem) {
        coroutineScope.launch {
            try {
                updateAisleUseCase(
                    Aisle(
                        name = aisle.name,
                        products = emptyList(),
                        locationId = aisle.locationId,
                        isDefault = aisle.isDefault,
                        rank = aisle.rank,
                        id = aisle.id
                    )
                )
            } catch (e: AisleronException) {
                _shoppingListUiState.value = ShoppingListUiState.Error(e.exceptionCode, e.message)

            } catch (e: Exception) {
                _shoppingListUiState.value =
                    ShoppingListUiState.Error(AisleronException.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    fun updateItemRank(item: ShoppingListItem) {
        coroutineScope.launch {
            (item as ShoppingListItemViewModel).updateRank()
        }
    }

    fun submitProductSearch(query: String) {
        _listFilter = { sli ->
            (sli is AisleShoppingListItem) || (
                    (sli is ProductShoppingListItem)
                            && (sli.name.contains(query, true))
                    )
        }
        coroutineScope.launch {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            val searchResults = shoppingList.filter(_listFilter)
            _shoppingListUiState.value = ShoppingListUiState.Updated(searchResults)
        }
    }

    fun requestDefaultList() {
        _listFilter = getListFilterByProductFilter(_defaultFilter)
        coroutineScope.launch {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            _shoppingListUiState.value =
                ShoppingListUiState.Updated(shoppingList.filter(_listFilter))
        }
    }

    fun removeItem(item: ShoppingListItem) {
        coroutineScope.launch {
            try {
                (item as ShoppingListItemViewModel).remove()
            } catch (e: AisleronException) {
                _shoppingListUiState.value = ShoppingListUiState.Error(e.exceptionCode, e.message)

            } catch (e: Exception) {
                _shoppingListUiState.value =
                    ShoppingListUiState.Error(AisleronException.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    sealed class ShoppingListUiState {
        data object Empty : ShoppingListUiState()
        data object Loading : ShoppingListUiState()
        data class Error(val errorCode: String, val errorMessage: String?) : ShoppingListUiState()
        data class Updated(val shoppingList: List<ShoppingListItem>) : ShoppingListUiState()
    }
}