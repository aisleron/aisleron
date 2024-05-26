package com.aisleron.domain.shoppinglist.usecase

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetShoppingListUseCase(private val locationRepository: LocationRepository) {
    operator fun invoke(locationId: Int): Flow<Location?> {
        return locationRepository.getLocationWithAislesWithProducts(locationId)
    }
}