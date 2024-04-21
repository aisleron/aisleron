package com.aisleron.domain.location.usecase

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

class UpdateLocationUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(location: Location) {
        locationRepository.update(location)
    }
}