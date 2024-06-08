package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.usecase.GetLocationUseCase

class UpdateAisleUseCase(
    private val aisleRepository: AisleRepository,
    private val getLocationUseCase: GetLocationUseCase
) {
    suspend operator fun invoke(aisle: Aisle) {
        getLocationUseCase(aisle.locationId)
            ?: throw AisleronException.InvalidLocationException("Invalid Location Id provided")

        aisleRepository.update(aisle)
    }
}