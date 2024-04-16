package com.aisleron.domain.aisle.usecases

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

class AddAisleUseCase(
    private val aisleRepository: AisleRepository
) {
    suspend operator fun invoke(aisle: Aisle): Int {
        return aisleRepository.add(aisle)
    }
}