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
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.Product
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
                    )
        }
    }

    private fun refreshListItems(locationId: Int) {
        coroutineScope.launch {
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

    fun updateAisle(aisle: ShoppingListItemViewModel) {
        coroutineScope.launch {
            try {
                updateAisleUseCase(
                    Aisle(
                        name = aisle.name,
                        products = emptyList(),
                        locationId = _locationId,
                        isDefault = aisle.inStock,
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

    fun updateItemRank(item: ShoppingListItemViewModel) {
        coroutineScope.launch {
            when (item.lineItemType) {
                ShoppingListItemType.AISLE -> updateAisleRanks(item)
                ShoppingListItemType.PRODUCT -> updateProductRank(item)
            }
        }
    }

    private suspend fun updateProductRank(product: ShoppingListItemViewModel) {
        updateAisleProductRankUseCase(
            AisleProduct(
                rank = product.rank,
                aisleId = product.aisleId,
                id = product.mappingId,
                product = Product(
                    id = product.id,
                    name = product.name,
                    inStock = product.inStock
                )
            )
        )
    }

    private suspend fun updateAisleRanks(aisle: ShoppingListItemViewModel) {
        updateAisleRankUseCase(
            Aisle(
                id = aisle.id,
                name = aisle.name,
                products = emptyList(),
                locationId = _locationId,
                rank = aisle.rank,
                isDefault = aisle.inStock
            )
        )
    }

    fun submitProductSearch(query: String) {
        _listFilter = { sli ->
            (sli.lineItemType == ShoppingListItemType.AISLE) || (
                    (sli.lineItemType == ShoppingListItemType.PRODUCT)
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

    fun removeItem(item: ShoppingListItemViewModel) {
        coroutineScope.launch {
            try {
                when (item.lineItemType) {
                    ShoppingListItemType.PRODUCT -> removeProductUseCase(item.id)
                    ShoppingListItemType.AISLE -> {
                        val aisle = getAisleUseCase(item.id)
                        aisle?.let { removeAisleUseCase(it) }
                    }
                }

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
        data object Success : ShoppingListUiState()
        data class Error(val errorCode: String, val errorMessage: String?) : ShoppingListUiState()
        data class Updated(val shoppingList: List<ShoppingListItemViewModel>) :
            ShoppingListUiState()
    }
}