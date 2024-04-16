package com.aisleron.domain.location.usecases

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

class GetLocationUseCase(
    private val locationRepository: LocationRepository,

) {
    suspend operator fun invoke(id: Int): Location? {
        return locationRepository.get(id)
    }
}