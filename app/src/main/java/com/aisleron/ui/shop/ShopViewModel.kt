package com.aisleron.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: LocationRepository) : ViewModel() {
    val pinned: Boolean? get() = location?.pinned
    val defaultFilter: FilterType? get() = location?.defaultFilter
    val type: LocationType? get() = location?.type
    val locationName: String? get() = location?.name

    private var location: Location? = null

    fun hydrate(locationId: Int) {
        viewModelScope.launch() {
            location = repository.get(locationId)
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
        viewModelScope.launch() {
            location!!.let {
                it.name = name
                it.pinned = pinned
            }
            repository.update(location!!)
        }
    }

    private fun addLocation(name: String, pinned: Boolean) {
        viewModelScope.launch() {
            val id = repository.add(
                Location(
                    type = LocationType.SHOP,
                    defaultFilter = FilterType.NEEDED,
                    name = name,
                    pinned = pinned,
                    id = 0
                )
            )
            hydrate(id)
        }
    }
}