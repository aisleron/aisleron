package com.aisleron.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopViewModel(
    private val addLocationUseCase: AddLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModel() {
    val pinned: Boolean get() = location?.pinned ?: false
    val defaultFilter: FilterType? get() = location?.defaultFilter
    val type: LocationType? get() = location?.type
    val locationName: String? get() = location?.name

    private var location: Location? = null

    private val _shopUiState = MutableStateFlow<ShopUiState>(ShopUiState.Empty)
    val shopUiState: StateFlow<ShopUiState> = _shopUiState

    fun hydrate(locationId: Int) {
        viewModelScope.launch {
            _shopUiState.value = ShopUiState.Loading
            location = getLocationUseCase(locationId)
            _shopUiState.value = ShopUiState.Updated(this@ShopViewModel)
        }
    }

    fun saveLocation(name: String, pinned: Boolean) {
        viewModelScope.launch {
            _shopUiState.value = ShopUiState.Loading
            if (location == null) {
                addLocation(name, pinned)
            } else {
                updateLocation(name, pinned)
            }
            _shopUiState.value = ShopUiState.Success
        }
    }

    private suspend fun updateLocation(name: String, pinned: Boolean) {
        location!!.let {
            it.name = name
            it.pinned = pinned
        }
        updateLocationUseCase(location!!)
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
        data object Error : ShopUiState()
        data object Success : ShopUiState()
        data class Updated(val product: ShopViewModel) : ShopUiState()
    }
}