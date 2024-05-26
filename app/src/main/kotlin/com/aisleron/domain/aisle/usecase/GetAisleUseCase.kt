package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

class GetAisleUseCase(
    private val aisleRepository: AisleRepository
) {
    suspend operator fun invoke(id: Int): Aisle? {
        return aisleRepository.get(id)
    }
}