package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.base.AisleronException

interface RemoveAisleUseCase {
    suspend operator fun invoke(aisle: Aisle)
}

class RemoveAisleUseCaseImpl(
    private val aisleRepository: AisleRepository,
    private val updateAisleProductsUseCase: UpdateAisleProductsUseCase,
    private val removeProductsFromAisleUseCase: RemoveProductsFromAisleUseCase
) : RemoveAisleUseCase {
    override suspend operator fun invoke(aisle: Aisle) {
        if (aisle.isDefault) {
            throw AisleronException.DeleteDefaultAisleException("Cannot delete default Aisle")
        }

        val aisleWithProducts = aisleRepository.getWithProducts(aisle.id)

        val defaultAisle = aisleRepository.getDefaultAisleFor(aisleWithProducts.locationId)
        if (defaultAisle != null) {
            val aisleProducts = aisleWithProducts.products
            aisleProducts.forEach { it.aisleId = defaultAisle.id }
            updateAisleProductsUseCase(aisleProducts)
        } else {
            removeProductsFromAisleUseCase(aisleWithProducts)
        }
        aisleRepository.remove(aisleWithProducts)
    }
}
