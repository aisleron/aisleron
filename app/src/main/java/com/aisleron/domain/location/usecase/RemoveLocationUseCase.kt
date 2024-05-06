package com.aisleron.domain.location.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository

class RemoveLocationUseCase(
    private val locationRepository: LocationRepository,
    private val removeAisleUseCase: RemoveAisleUseCase,
    private val removeDefaultAisleUseCase: RemoveDefaultAisleUseCase
) {
    suspend operator fun invoke(location: Location) {
        val loc = locationRepository.getLocationWithAisles(location.id)
        val aisles = loc.aisles.filter { !it.isDefault }
        aisles.forEach { removeAisleUseCase(it) }

        val defaultAisle: Aisle? = loc.aisles.firstOrNull { it.isDefault }
        defaultAisle?.let {
            removeDefaultAisleUseCase(defaultAisle)
        }

        locationRepository.remove(loc)
    }
}
