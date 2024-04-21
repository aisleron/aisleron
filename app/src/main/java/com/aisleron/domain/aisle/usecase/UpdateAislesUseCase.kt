package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

class UpdateAislesUseCase(
    private val aisleRepository: AisleRepository
) {
    suspend operator fun invoke(aisles: List<Aisle>) {
        aisleRepository.update(aisles)
    }
}