package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository

class UpdateAisleProductRankUseCase(
    private val aisleProductRepository: AisleProductRepository
) {
    suspend operator fun invoke(aisleProduct: AisleProduct) {
        aisleProductRepository.updateAisleProductRank(aisleProduct)
    }
}