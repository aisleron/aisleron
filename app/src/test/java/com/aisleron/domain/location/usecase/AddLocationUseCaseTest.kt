package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddLocationUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var addLocationUseCase: AddLocationUseCase
    private lateinit var existingLocation: Location

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val locationRepository = testData.getRepository<LocationRepository>()

        addLocationUseCase = AddLocationUseCaseImpl(
            locationRepository,
            AddAisleUseCaseImpl(
                testData.getRepository<AisleRepository>(), GetLocationUseCase(locationRepository)
            ),
            GetAllProductsUseCase(testData.getRepository<ProductRepository>()),
            AddAisleProductsUseCase(testData.getRepository<AisleProductRepository>()),
            IsLocationNameUniqueUseCase(locationRepository)
        )

        existingLocation = runBlocking { locationRepository.get(1)!! }
    }

    @Test
    fun addLocation_IsDuplicateName_ThrowsException() {
        val newLocation = existingLocation.copy(id = 0, pinned = !existingLocation.pinned)
        runBlocking {
            assertThrows<AisleronException.DuplicateLocationNameException> {
                addLocationUseCase(newLocation)
            }
        }
    }

    @Test
    fun addLocation_IsExistingLocation_ThrowsException() {
        val updateLocation =
            existingLocation.copy(
                name = existingLocation.name + " Updated",
                pinned = !existingLocation.pinned
            )

        runBlocking {
            assertThrows<AisleronException.DuplicateLocationException> {
                addLocationUseCase(updateLocation)
            }
        }
    }

    private fun getNewLocation(): Location {
        val newLocation = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "Shop Add New Location",
            pinned = false,
            aisles = emptyList()
        )
        return newLocation
    }

    @Test
    fun addLocation_IsNewLocation_LocationCreated() {
        val newLocation = getNewLocation()
        val countBefore: Int
        val countAfter: Int
        val insertedLocation: Location?
        runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            countBefore = locationRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            insertedLocation = locationRepository.get(id)
            countAfter = locationRepository.getAll().count()
        }
        assertNotNull(insertedLocation)
        assertEquals(countBefore + 1, countAfter)
        assertEquals(newLocation.name, insertedLocation?.name)
        assertEquals(newLocation.type, insertedLocation?.type)
        assertEquals(newLocation.pinned, insertedLocation?.pinned)
        assertEquals(newLocation.defaultFilter, insertedLocation?.defaultFilter)
    }

    @Test
    fun addLocation_LocationInserted_AddsDefaultAisle() {
        val newLocation = getNewLocation()
        val aisleCountBefore: Int
        val aisleCountAfter: Int
        val defaultAisle: Aisle?
        runBlocking {
            val aisleRepository = testData.getRepository<AisleRepository>()
            aisleCountBefore = aisleRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            aisleCountAfter = aisleRepository.getAll().count()
            defaultAisle = aisleRepository.getDefaultAisleFor(id)
        }
        assertNotNull(defaultAisle)
        assertEquals(aisleCountBefore + 1, aisleCountAfter)
    }

    @Test
    fun addLocation_LocationInserted_AddsAisleProducts() {
        val newLocation = getNewLocation()
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val productCount: Int
        val aisleProducts: List<AisleProduct>
        val defaultAisle: Aisle?
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            productCount = testData.getRepository<ProductRepository>().getAll().count()
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
            defaultAisle = testData.getRepository<AisleRepository>().getDefaultAisleFor(id)
            aisleProducts =
                aisleProductRepository.getAll().filter { it.aisleId == defaultAisle?.id }
        }
        assertEquals(productCount, aisleProducts.count())
        assertEquals(aisleProductCountBefore + productCount, aisleProductCountAfter)
    }
}