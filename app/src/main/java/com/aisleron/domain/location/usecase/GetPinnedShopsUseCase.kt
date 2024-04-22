package com.aisleron.domain.location.usecase

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.flow.Flow

class GetPinnedShopsUseCase(private val locationRepository: LocationRepository) {
    operator fun invoke(): Flow<List<Location>> {
        return locationRepository.getPinnedShops()
    }
}