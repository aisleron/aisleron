package com.aisleron.domain.location.usecases

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleProduct
import com.aisleron.domain.aisle.usecases.AddAisleUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.usecases.GetProductsUseCase

class AddLocationUseCase(
    private val locationRepository: LocationRepository,
    private val addAisleUseCase: AddAisleUseCase,
    private val getProductsUseCase: GetProductsUseCase

) {
    suspend operator fun invoke(location: Location): Int {
        val newLocationId = locationRepository.add(location)
        val newAisleId = addAisleUseCase(
            Aisle(
                id = 0,
                name = "No Aisle",
                locationId = newLocationId,
                rank = 0,
                isDefault = true,
                products = getProductsUseCase().sortedBy { it.name }.mapIndexed { i, p ->
                    AisleProduct(
                        rank = (i + 1) * 100,
                        aisleId = 0,
                        product = p
                    )
                }
            )
        )
        //TODO: Should there be an AddProductsToAisleUseCase?
        return newLocationId
    }
}