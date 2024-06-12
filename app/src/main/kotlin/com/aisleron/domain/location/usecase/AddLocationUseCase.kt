package com.aisleron.domain.location.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.usecase.GetAllProductsUseCase

interface AddLocationUseCase {
    suspend operator fun invoke(location: Location): Int
}

class AddLocationUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val addAisleUseCase: AddAisleUseCase,
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val addAisleProductsUseCase: AddAisleProductsUseCase,
    private val isLocationNameUniqueUseCase: IsLocationNameUniqueUseCase

) : AddLocationUseCase {
    override suspend operator fun invoke(location: Location): Int {

        if (!isLocationNameUniqueUseCase(location)) {
            throw AisleronException.DuplicateLocationNameException("Location Name must be unique")
        }

        val newLocationId = locationRepository.add(location)
        //Add location default Aisle. Set Rank high so it shows at the end of the shopping list
        val newAisleId = addAisleUseCase(
            Aisle(
                id = 0,
                name = "No Aisle",
                locationId = newLocationId,
                rank = 1000,
                isDefault = true,
                products = emptyList()
            )
        )

        addAisleProductsUseCase(
            getAllProductsUseCase().sortedBy { it.name }.mapIndexed { _, p ->
                AisleProduct(
                    rank = 0,
                    aisleId = newAisleId,
                    product = p,
                    id = 0
                )
            })

        return newLocationId
    }
}