package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase

class RemoveAisleUseCase(
    private val aisleRepository: AisleRepository,
    private val updateAisleProductsUseCase: UpdateAisleProductsUseCase,
    private val removeProductsFromAisleUseCase: RemoveProductsFromAisleUseCase
) {
    suspend operator fun invoke(aisle: Aisle) {
        //TODO: Throw isDefault exception here; catch error in viewmodel, not in fragment, because
        // this is running in a coroutine
        /*if (aisle.isDefault) {
            throw AisleException.DeleteDefaultAisleException()
        }*/

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
