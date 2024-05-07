package com.aisleron.domain.location.usecase

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

class UpdateLocationUseCase(
    private val locationRepository: LocationRepository,
    private val checkLocationNameIsUniqueUseCase: CheckLocationNameIsUniqueUseCase
) {
    suspend operator fun invoke(location: Location) {

        if (!checkLocationNameIsUniqueUseCase(location)) {
            throw AisleronException.DuplicateProductNameException("Location Name must be unique")
        }

        locationRepository.update(location)
    }
}