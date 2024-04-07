package com.aisleron.domain.shoppinglist

import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationRepository

class GetShoppingListUseCase(private val locationRepository: LocationRepository) {
    operator fun invoke(locationId: Int, filterType: FilterType) {
        TODO("Implement")
    }
}