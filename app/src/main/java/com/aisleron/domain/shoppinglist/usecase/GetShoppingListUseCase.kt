package com.aisleron.domain.shoppinglist.usecase

import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

class GetShoppingListUseCase(private val locationRepository: LocationRepository) {
    suspend operator fun invoke(locationId: Int, filterType: FilterType): Location? {
        return locationRepository.getLocationWithAislesWithProducts(locationId)
    }
}