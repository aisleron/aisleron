package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

class GetDefaultAislesUseCase(private val aisleRepository: AisleRepository) {
    suspend operator fun invoke(): List<Aisle> {
        return aisleRepository.getDefaultAisles()
    }
}
