package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.AisleProduct
import com.aisleron.domain.aisle.AisleRepository

class AddAisleProductsUseCase(
    private val aisleRepository: AisleRepository
) {
    suspend operator fun invoke(aisleProducts: List<AisleProduct>) {
        return aisleRepository.addAisleProducts(aisleProducts)
    }
}