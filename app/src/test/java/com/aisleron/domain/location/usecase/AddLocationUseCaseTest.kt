package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
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

        addLocationUseCase = AddLocationUseCaseImpl(
            testData.locationRepository,
            AddAisleUseCaseImpl(
                testData.aisleRepository,
                GetLocationUseCase(testData.locationRepository)
            ),
            GetAllProductsUseCase(testData.productRepository),
            AddAisleProductsUseCase(testData.aisleProductRepository),
            IsLocationNameUniqueUseCase(testData.locationRepository)
        )

        existingLocation = runBlocking {
            testData.locationRepository.get(1)!!
        }
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
            countBefore = testData.locationRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            insertedLocation = testData.locationRepository.get(id)
            countAfter = testData.locationRepository.getAll().count()
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
            aisleCountBefore = testData.aisleRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            aisleCountAfter = testData.aisleRepository.getAll().count()
            defaultAisle = testData.aisleRepository.getDefaultAisleFor(id)
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
            productCount = testData.productRepository.getAll().count()
            aisleProductCountBefore = testData.aisleProductRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            aisleProductCountAfter = testData.aisleProductRepository.getAll().count()
            defaultAisle = testData.aisleRepository.getDefaultAisleFor(id)
            aisleProducts =
                testData.aisleProductRepository.getAll().filter { it.aisleId == defaultAisle?.id }
        }
        assertEquals(productCount, aisleProducts.count())
        assertEquals(aisleProductCountBefore + productCount, aisleProductCountAfter)
    }
}