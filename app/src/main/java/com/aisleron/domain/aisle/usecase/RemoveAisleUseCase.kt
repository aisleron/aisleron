package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase

class RemoveAisleUseCase(
    private val aisleRepository: AisleRepository,
    private val updateAisleProductsUseCase: UpdateAisleProductsUseCase
) {
    suspend operator fun invoke(aisleId: Int) {
        val aisle = aisleRepository.getWithProducts(aisleId)
        val defaultAisle = aisleRepository.getDefaultAisleFor(aisle.locationId)
        val aisleProducts = aisle.products
        aisleProducts.forEach { it.aisleId = defaultAisle.id }
        updateAisleProductsUseCase(aisleProducts)
        aisleRepository.remove(aisle)
    }
}