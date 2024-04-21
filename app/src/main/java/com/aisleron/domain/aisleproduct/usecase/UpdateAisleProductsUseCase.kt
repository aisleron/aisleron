package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository

class UpdateAisleProductsUseCase(
    private val aisleProductRepository: AisleProductRepository
) {
    suspend operator fun invoke(aisleProducts: List<AisleProduct>) {
        aisleProductRepository.update(aisleProducts)
    }
}