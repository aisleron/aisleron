package com.aisleron.ui.shoplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.GetPinnedShopsUseCase
import com.aisleron.domain.location.usecase.GetShopsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopListViewModel(
    private val getShopsUseCase: GetShopsUseCase,
    private val getPinnedShopsUseCase: GetPinnedShopsUseCase
) : ViewModel() {

    private val _shopListUiState = MutableStateFlow<ShopListUiState>(ShopListUiState.Empty)
    val shopListUiState: StateFlow<ShopListUiState> = _shopListUiState

    fun hydratePinnedShops() {
        hydrateShops(getPinnedShopsUseCase())
    }

    fun hydrateAllShops() {
        hydrateShops(getShopsUseCase())
    }

    private fun hydrateShops(shopsFlow: Flow<List<Location>>) {
        viewModelScope.launch {
            _shopListUiState.value = ShopListUiState.Loading
            shopsFlow.collect {
                val items = it.map { l ->
                    ShopListItemViewModel(
                        id = l.id,
                        defaultFilter = l.defaultFilter,
                        name = l.name
                    )
                }
                _shopListUiState.value = ShopListUiState.Success(items)
            }
        }
    }

    sealed class ShopListUiState {
        data object Empty : ShopListUiState()
        data object Loading : ShopListUiState()
        data object Error : ShopListUiState()
        data class Success(val shops: List<ShopListItemViewModel>) : ShopListUiState()
    }
}