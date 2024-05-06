package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase

class RemoveDefaultAisleUseCase(
    private val aisleRepository: AisleRepository,
    private val removeProductsFromAisleUseCase: RemoveProductsFromAisleUseCase
) {
    suspend operator fun invoke(aisle: Aisle) {
        removeProductsFromAisleUseCase(aisle)
        aisleRepository.remove(aisle)
    }
}
