package com.aisleron.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import kotlinx.coroutines.launch

class ShopViewModel(
    private val addLocationUseCase: AddLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModel() {
    val pinned: Boolean? get() = location?.pinned
    val defaultFilter: FilterType? get() = location?.defaultFilter
    val type: LocationType? get() = location?.type
    val locationName: String? get() = location?.name

    private var location: Location? = null

    fun hydrate(locationId: Int) {
        viewModelScope.launch {
            location = getLocationUseCase(locationId)
        }
    }

    fun saveLocation(name: String, pinned: Boolean) {
        if (location == null) {
            addLocation(name, pinned)
        } else {
            updateLocation(name, pinned)
        }
    }

    private fun updateLocation(name: String, pinned: Boolean) {
        viewModelScope.launch {
            location!!.let {
                it.name = name
                it.pinned = pinned
            }
            updateLocationUseCase(location!!)
        }
    }

    private fun addLocation(name: String, pinned: Boolean) {
        viewModelScope.launch {
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
    }
}