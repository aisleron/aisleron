package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows

class CopyLocationUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var copyLocationUseCase: CopyLocationUseCase
    private lateinit var existingLocation: Location

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val locationRepository = testData.getRepository<LocationRepository>()

        copyLocationUseCase = CopyLocationUseCaseImpl(
            locationRepository,
            testData.getRepository<AisleRepository>(),
            testData.getRepository<AisleProductRepository>(),
            IsLocationNameUniqueUseCase(locationRepository)
        )

        existingLocation = runBlocking { locationRepository.getShops().first().first() }
    }

    @Test
    fun copyLocation_IsDuplicateName_ThrowsException() = runTest {
        val existingName = "Existing Shop Name"
        val locationRepository = testData.getRepository<LocationRepository>()
        locationRepository.add(existingLocation.copy(id = 0, name = existingName))

        assertThrows<AisleronException.DuplicateLocationNameException> {
            copyLocationUseCase(existingLocation, existingName)
        }
    }

    @Test
    fun copyLocation_IsValidName_LocationCreated() = runTest {
        val newName = "Copied Shop Name"

        val newLocationId = copyLocationUseCase(existingLocation, newName)

        val locationRepository = testData.getRepository<LocationRepository>()
        val new = locationRepository.getLocationWithAislesWithProducts(newLocationId).first()
        assertNotNull(new)
        assertEquals(newName, new.name)

        val source =
            locationRepository.getLocationWithAislesWithProducts(existingLocation.id).first()

        assertEquals(source?.aisles?.count(), new.aisles.count())
        assertEquals(flattenProducts(source?.aisles!!).count(), flattenProducts(new.aisles).count())
    }

    private fun flattenProducts(aisles: List<Aisle>): List<Product> =
        aisles.flatMap { aisle ->
            aisle.products.map { it.product }
        }
}