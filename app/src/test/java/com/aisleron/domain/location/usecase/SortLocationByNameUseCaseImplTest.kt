package com.aisleron.domain.location.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.IsAisleNameUniqueUseCase
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.domain.product.usecase.AddProductUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SortLocationByNameUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var sortLocationByNameUseCase: SortLocationByNameUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        sortLocationByNameUseCase = dm.getUseCase()
    }

    @Test
    fun sortLocationByName_WithAisles_AislesSorted() = runTest {
        val locationRepository = dm.getRepository<LocationRepository>()
        val aisleRepository = dm.getRepository<AisleRepository>()
        val locationId = locationRepository.getShops().first().first().id
        val addAisleUseCase = AddAisleUseCaseImpl(
            aisleRepository,
            GetLocationUseCase(locationRepository),
            IsAisleNameUniqueUseCase(aisleRepository)
        )

        val aisle = Aisle(
            name = "ZZZ",
            products = emptyList(),
            locationId = locationId,
            rank = 2000,
            isDefault = false,
            id = 0,
            expanded = true
        )

        addAisleUseCase(aisle)
        addAisleUseCase(aisle.copy(name = "AAA", rank = 2002))

        sortLocationByNameUseCase(locationId)

        val aisles =
            locationRepository.getLocationWithAislesWithProducts(locationId).first()!!.aisles

        assertEquals(1, aisles.first { it.name == "AAA" }.rank)
        assertEquals(aisles.maxOf { it.rank } - 1, aisles.first { it.name == "ZZZ" }.rank)
        assertEquals(aisles.maxOf { it.rank }, aisles.first { it.isDefault }.rank)
    }

    @Test
    fun sortLocationByName_WithProducts_ProductsSorted() = runTest {
        val locationRepository = dm.getRepository<LocationRepository>()
        val locationId = locationRepository.getShops().first().first().id
        val addProductUseCase = dm.getUseCase<AddProductUseCase>()

        val product = Product(
            id = 0,
            name = "ZZZ",
            inStock = false,
            qtyNeeded = 0.0,
            noteId = null,
            qtyIncrement = 1.0,
            trackingMode = TrackingMode.DEFAULT,
            unitOfMeasure = "Qty"
        )

        addProductUseCase(product)
        addProductUseCase(product.copy(name = "AAA"))

        sortLocationByNameUseCase(locationId)

        val sortedProducts =
            locationRepository.getLocationWithAislesWithProducts(locationId)
                .first()!!.aisles.first { it.isDefault }.products

        assertEquals(1, sortedProducts.first { it.product.name == "AAA" }.rank)
        assertEquals(
            sortedProducts.maxOf { it.rank }, sortedProducts.first { it.product.name == "ZZZ" }.rank
        )
    }
}


