package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisle.usecase.IsAisleNameUniqueUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.GetAisleMaxRankUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SortLocationByNameUseCaseImplTest {

    private lateinit var testData: TestDataManager
    private lateinit var sortLocationByNameUseCase: SortLocationByNameUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val aisleRepository = testData.getRepository<AisleRepository>()
        sortLocationByNameUseCase = SortLocationByNameUseCaseImpl(
            locationRepository = testData.getRepository<LocationRepository>(),
            updateAisleUseCase = UpdateAisleUseCaseImpl(
                aisleRepository = aisleRepository,
                getLocationUseCase = GetLocationUseCase(testData.getRepository<LocationRepository>()),
                isAisleNameUniqueUseCase = IsAisleNameUniqueUseCase(aisleRepository)
            ),
            updateAisleProductUseCase = UpdateAisleProductsUseCase(testData.getRepository<AisleProductRepository>())
        )
    }

    @Test
    fun sortLocationByName_WithAisles_AislesSorted() = runTest {
        val locationRepository = testData.getRepository<LocationRepository>()
        val aisleRepository = testData.getRepository<AisleRepository>()
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
        val locationRepository = testData.getRepository<LocationRepository>()
        val locationId = locationRepository.getShops().first().first().id
        val addProductUseCase = AddProductUseCaseImpl(
            testData.getRepository<ProductRepository>(),
            GetDefaultAislesUseCase(testData.getRepository<AisleRepository>()),
            AddAisleProductsUseCase(testData.getRepository<AisleProductRepository>()),
            IsProductNameUniqueUseCase(testData.getRepository<ProductRepository>()),
            GetAisleMaxRankUseCase(testData.getRepository<AisleProductRepository>())
        )

        val product = Product(
            id = 0,
            name = "ZZZ",
            inStock = false,
            qtyNeeded = 0
        )

        val aisle = locationRepository.getLocationWithAisles(locationId).aisles.first()
        addProductUseCase(product, aisle)
        addProductUseCase(product.copy(name = "AAA"), aisle)

        sortLocationByNameUseCase(locationId)

        val sortedProducts =
            locationRepository.getLocationWithAislesWithProducts(locationId)
                .first()!!.aisles.first { it.id == aisle.id }.products

        assertEquals(1, sortedProducts.first { it.product.name == "AAA" }.rank)
        assertEquals(
            sortedProducts.maxOf { it.rank }, sortedProducts.first { it.product.name == "ZZZ" }.rank
        )
    }
}


