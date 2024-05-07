package com.aisleron.domain.location.usecase

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

class CheckLocationNameIsUniqueUseCase(private val locationRepository: LocationRepository) {
    suspend operator fun invoke(location: Location): Boolean {
        val existingLocation: Location? = locationRepository.getByName(location.name.trim())
        //Location name is unique if no existing location was found, the existing location has
        // the same id, or the two locations are of different types
        return existingLocation?.let { it.id == location.id || it.type != location.type } ?: true

    }
}