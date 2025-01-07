package com.aisleron.domain.location.usecase

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

class GetHomeLocationUseCase(private val locationRepository: LocationRepository) {
    suspend operator fun invoke(): Location {
        return locationRepository.getHome()
    }
}