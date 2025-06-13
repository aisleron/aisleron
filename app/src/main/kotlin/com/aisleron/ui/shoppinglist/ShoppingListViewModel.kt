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
    private val shoppingList = mutableListOf<ShoppingListItem>()
    private var _showDefaultAisle: Boolean = true

    private var _locationName: String = ""
    val locationName: String get() = _locationName

    private var _locationType: LocationType = LocationType.HOME
    val locationType: LocationType get() = _locationType

    private var _defaultFilter: FilterType = FilterType.NEEDED
    val defaultFilter: FilterType get() = _defaultFilter

    private var _locationId: Int = 0
    val locationId: Int get() = _locationId

    private lateinit var _listFilter: (ShoppingListItem) -> Boolean
    private val _shoppingListUiState = MutableStateFlow<ShoppingListUiState>(
        ShoppingListUiState.Empty
    )

    val shoppingListUiState = _shoppingListUiState.asStateFlow()

    fun hydrate(locationId: Int, filterType: FilterType) {
        _defaultFilter = filterType
        refreshListItems(locationId)
    }

    private fun isValidAisleSli(sli: AisleShoppingListItem): Boolean {
        return true
    }

    private fun isValidProductSli(
        sli: ProductShoppingListItem, filter: FilterType, productNameFilter: String
    ): Boolean {
        return ((sli.inStock && filter == FilterType.IN_STOCK)
                || (!sli.inStock && filter == FilterType.NEEDED)
                || (filter == FilterType.ALL)
                ) &&
                (productNameFilter == "" || (sli.name.contains(productNameFilter.trim(), true)))
    }

    private fun buildListFilter(
        filter: FilterType, showDefaultAisle: Boolean, productNameFilter: String = ""
    ): (ShoppingListItem) -> Boolean {
        return { sli: ShoppingListItem ->
            (showDefaultAisle || !sli.isDefaultAisle) &&
                    when (sli) {
                        is AisleShoppingListItem -> isValidAisleSli(sli)
                        is ProductShoppingListItem -> isValidProductSli(
                            sli, filter, productNameFilter
                        )

                        else -> false
                    }
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
                    _showDefaultAisle = it.showDefaultAisle

                    it.aisles.forEach { a ->
                        shoppingList.add(
                            AisleShoppingListItemViewModel(
                                rank = a.rank,
                                id = a.id,
                                name = a.name,
                                isDefaultAisle = a.isDefault,
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
                            ProductShoppingListItemViewModel(
                                aisleRank = a.rank,
                                rank = p.rank,
                                id = p.product.id,
                                name = p.product.name,
                                inStock = p.product.inStock,
                                aisleId = p.aisleId,
                                aisleProductId = p.id,
                                removeProductUseCase = removeProductUseCase,
                                updateAisleProductRankUseCase = updateAisleProductRankUseCase,
                                isDefaultAisle = a.isDefault
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

                if (!::_listFilter.isInitialized) _listFilter =
                    buildListFilter(_defaultFilter, _showDefaultAisle)

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
                    ShoppingListUiState.Error(
                        AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                    )
            }
        }
    }

    fun updateAisleName(aisle: AisleShoppingListItem, newName: String) {
        coroutineScope.launch {
            try {
                updateAisleUseCase(
                    Aisle(
                        name = newName,
                        products = emptyList(),
                        locationId = aisle.locationId,
                        isDefault = aisle.isDefaultAisle,
                        rank = aisle.rank,
                        id = aisle.id
                    )
                )
            } catch (e: AisleronException) {
                _shoppingListUiState.value = ShoppingListUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _shoppingListUiState.value =
                    ShoppingListUiState.Error(
                        AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                    )
            }
        }
    }

    fun updateItemRank(item: ShoppingListItem, precedingItem: ShoppingListItem?) {
        coroutineScope.launch {
            (item as ShoppingListItemViewModel).updateRank(precedingItem)
        }
    }

    fun submitProductSearch(productNameFilter: String) {
        _listFilter = buildListFilter(FilterType.ALL, true, productNameFilter)
        coroutineScope.launch {
            _shoppingListUiState.value = ShoppingListUiState.Loading
            val searchResults = shoppingList.filter(_listFilter)
            _shoppingListUiState.value = ShoppingListUiState.Updated(searchResults)
        }
    }

    fun requestDefaultList() {
        _listFilter = buildListFilter(_defaultFilter, _showDefaultAisle)
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
                    ShoppingListUiState.Error(
                        AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                    )
            }
        }
    }

    sealed class ShoppingListUiState {
        data object Empty : ShoppingListUiState()
        data object Loading : ShoppingListUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ShoppingListUiState()

        data class Updated(val shoppingList: List<ShoppingListItem>) : ShoppingListUiState()
    }
}