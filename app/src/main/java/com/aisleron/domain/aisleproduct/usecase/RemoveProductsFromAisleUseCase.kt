package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisleproduct.AisleProductRepository

class RemoveProductsFromAisleUseCase(
    private val aisleProductRepository: AisleProductRepository
) {
    suspend operator fun invoke(aisle: Aisle) {
        aisleProductRepository.removeProductsFromAisle(aisle.id)
    }
}