package com.aisleron.ui.shoplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopListViewModel (private val repository: LocationRepository) : ViewModel() {
    private var _shops = mutableListOf<ShopListItemViewModel>()
    val shops: List<ShopListItemViewModel> = _shops

    private val _shopListUiState = MutableStateFlow<ShopListUiState>(ShopListUiState.Empty)
    val shopListUiState: StateFlow<ShopListUiState> = _shopListUiState


    fun hydrate() {
        viewModelScope.launch() {
            _shops.clear()
            _shopListUiState.value = ShopListUiState.Loading
            _shops += repository.getShops().map {
                ShopListItemViewModel(
                    id = it.id,
                    defaultFilter = it.defaultFilter,
                    name = it.name
                )
            }.toMutableList()

            _shopListUiState.value = ShopListUiState.Success(shops)
        }
    }

    sealed class ShopListUiState {
        data object Empty : ShopListUiState()
        data object Loading : ShopListUiState()

        //data object Error : ShopListUiState()
        data class Success(val shops: List<ShopListItemViewModel>) : ShopListUiState()
    }
}