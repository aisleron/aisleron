package com.aisleron.domain.location.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleProduct
import com.aisleron.domain.aisle.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.usecase.GetAllProductsUseCase

class AddLocationUseCase(
    private val locationRepository: LocationRepository,
    private val addAisleUseCase: AddAisleUseCase,
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val addAisleProductsUseCase: AddAisleProductsUseCase

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
                products = emptyList()
            )
        )

        addAisleProductsUseCase(
            getAllProductsUseCase().sortedBy { it.name }.mapIndexed { i, p ->
                AisleProduct(
                    rank = (i + 1) * 100,
                    aisleId = newAisleId,
                    product = p
                )
            })

        return newLocationId
    }
}