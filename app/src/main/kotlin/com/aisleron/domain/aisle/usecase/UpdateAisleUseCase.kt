package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

class UpdateAisleUseCase(
    private val aisleRepository: AisleRepository
) {
    suspend operator fun invoke(aisle: Aisle) {
        aisleRepository.update(aisle)
    }
}