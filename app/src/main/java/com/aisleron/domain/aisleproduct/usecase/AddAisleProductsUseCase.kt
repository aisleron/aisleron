package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository

class AddAisleProductsUseCase(
    private val aisleProductRepository: AisleProductRepository
) {
    suspend operator fun invoke(aisleProducts: List<AisleProduct>): List<Int> {
        return aisleProductRepository.add(aisleProducts)
    }
}