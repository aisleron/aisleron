package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

interface GetAisleUseCase {
    suspend operator fun invoke(id: Int): Aisle?
}

class GetAisleUseCaseImpl(
    private val aisleRepository: AisleRepository
) : GetAisleUseCase {
    override suspend operator fun invoke(id: Int): Aisle? {
        return aisleRepository.get(id)
    }
}