package com.aisleron.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: LocationRepository) : ViewModel() {
    var pinned: Boolean = true
    val locationName: String get() = location?.name ?: ""
    private var defaultFilter: FilterType = FilterType.NEEDED
    private var type: LocationType = LocationType.SHOP
    private var location: Location? = null

    fun loadLocation(locationId: Int) {
        viewModelScope.launch() {
            location = repository.get(locationId)
        }
    }

    fun saveLocation(name: String, pinned: Boolean) {
        viewModelScope.launch() {
            repository.add(
                Location(
                    type = type,
                    defaultFilter = defaultFilter,
                    name = name,
                    pinned = pinned,
                    id = 0
                )
            )
        }
    }
}