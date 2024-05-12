package com.aisleron.domain.location.usecase

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

class UpdateLocationUseCase(
    private val locationRepository: LocationRepository,
    private val isLocationNameUniqueUseCase: IsLocationNameUniqueUseCase
) {
    suspend operator fun invoke(location: Location) {

        if (!isLocationNameUniqueUseCase(location)) {
            throw AisleronException.DuplicateProductNameException("Location Name must be unique")
        }

        locationRepository.update(location)
    }
}