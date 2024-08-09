package com.aisleron.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopViewModel(
    private val addLocationUseCase: AddLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    val pinned: Boolean get() = location?.pinned ?: false
    val defaultFilter: FilterType? get() = location?.defaultFilter
    val type: LocationType? get() = location?.type
    val locationName: String? get() = location?.name

    private var location: Location? = null

    private val _shopUiState = MutableStateFlow<ShopUiState>(ShopUiState.Empty)
    val shopUiState: StateFlow<ShopUiState> = _shopUiState

    fun hydrate(locationId: Int) {
        coroutineScope.launch {
            _shopUiState.value = ShopUiState.Loading
            location = getLocationUseCase(locationId)
            _shopUiState.value = ShopUiState.Updated(this@ShopViewModel)
        }
    }

    fun saveLocation(name: String, pinned: Boolean) {
        coroutineScope.launch {
            _shopUiState.value = ShopUiState.Loading
            try {
                location?.let { updateLocation(it, name, pinned) } ?: addLocation(name, pinned)
                _shopUiState.value = ShopUiState.Success
            } catch (e: AisleronException) {
                _shopUiState.value = ShopUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _shopUiState.value =
                    ShopUiState.Error(AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    private suspend fun updateLocation(location: Location, name: String, pinned: Boolean) {
        val updateLocation = location.copy(name = name, pinned = pinned)
        updateLocationUseCase(updateLocation)
        hydrate(updateLocation.id)
    }

    private suspend fun addLocation(name: String, pinned: Boolean) {
        val id = addLocationUseCase(
            Location(
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = name,
                pinned = pinned,
                aisles = emptyList(),
                id = 0
            )
        )
        hydrate(id)
    }

    sealed class ShopUiState {
        data object Empty : ShopUiState()
        data object Loading : ShopUiState()
        data object Success : ShopUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ShopUiState()

        data class Updated(val shop: ShopViewModel) : ShopUiState()
    }
}