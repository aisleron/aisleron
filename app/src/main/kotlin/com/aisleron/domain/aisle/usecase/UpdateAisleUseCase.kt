package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.usecase.GetLocationUseCase

interface UpdateAisleUseCase {
    suspend operator fun invoke(aisle: Aisle)
}

class UpdateAisleUseCaseImpl(
    private val aisleRepository: AisleRepository,
    private val getLocationUseCase: GetLocationUseCase
) : UpdateAisleUseCase {
    override suspend operator fun invoke(aisle: Aisle) {
        getLocationUseCase(aisle.locationId)
            ?: throw AisleronException.InvalidLocationException("Invalid Location Id provided")

        aisleRepository.update(aisle)
    }
}