package com.aisleron.ui.shoplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.GetPinnedShopsUseCase
import com.aisleron.domain.location.usecase.GetShopsUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopListViewModel(
    private val getShopsUseCase: GetShopsUseCase,
    private val getPinnedShopsUseCase: GetPinnedShopsUseCase,
    private val removeLocationUseCase: RemoveLocationUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private val _shopListUiState = MutableStateFlow<ShopListUiState>(ShopListUiState.Empty)
    val shopListUiState: StateFlow<ShopListUiState> = _shopListUiState

    fun hydratePinnedShops() {
        hydrateShops(getPinnedShopsUseCase())
    }

    fun hydrateAllShops() {
        hydrateShops(getShopsUseCase())
    }

    private fun hydrateShops(shopsFlow: Flow<List<Location>>) {
        coroutineScope.launch {
            _shopListUiState.value = ShopListUiState.Loading
            shopsFlow.collect {
                val items = it.map { l ->
                    ShopListItemViewModel(
                        id = l.id,
                        defaultFilter = l.defaultFilter,
                        name = l.name
                    )
                }
                _shopListUiState.value = ShopListUiState.Updated(items)
            }
        }
    }

    fun removeItem(item: ShopListItemViewModel) {
        coroutineScope.launch {
            try {
                val location = getLocationUseCase(item.id)
                location?.let { removeLocationUseCase(location) }
            } catch (e: Exception) {
                _shopListUiState.value =
                    ShopListUiState.Error(AisleronException.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    sealed class ShopListUiState {
        data object Empty : ShopListUiState()
        data object Loading : ShopListUiState()
        data object Success : ShopListUiState()
        data class Error(val errorCode: String, val errorMessage: String?) : ShopListUiState()
        data class Updated(val shops: List<ShopListItemViewModel>) : ShopListUiState()
    }
}