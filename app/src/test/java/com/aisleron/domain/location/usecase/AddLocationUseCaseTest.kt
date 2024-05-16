package com.aisleron.domain.location.usecase

import com.aisleron.data.aisle.AisleDaoTestImpl
import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.aisle.AisleRepositoryImpl
import com.aisleron.data.aisleproduct.AisleProductDaoTestImpl
import com.aisleron.data.aisleproduct.AisleProductRankMapper
import com.aisleron.data.aisleproduct.AisleProductRepositoryImpl
import com.aisleron.data.location.LocationDaoTestImpl
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.data.product.ProductDaoTestImpl
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddLocationUseCaseTest {

    private lateinit var addLocationUseCase: AddLocationUseCase
    private lateinit var locationRepository: LocationRepositoryImpl
    private lateinit var existingLocation: Location

    private lateinit var aisleRepository: AisleRepositoryImpl
    private lateinit var aisleProductRepository: AisleProductRepositoryImpl
    private lateinit var productRepository: ProductRepositoryImpl

    @BeforeEach
    fun setUp() {
        locationRepository = LocationRepositoryImpl(LocationDaoTestImpl(), LocationMapper())
        aisleRepository = AisleRepositoryImpl(AisleDaoTestImpl(), AisleMapper())
        aisleProductRepository =
            AisleProductRepositoryImpl(AisleProductDaoTestImpl(), AisleProductRankMapper())
        productRepository =
            ProductRepositoryImpl(ProductDaoTestImpl(), AisleProductDaoTestImpl(), ProductMapper())

        addLocationUseCase = AddLocationUseCase(
            locationRepository,
            AddAisleUseCase(aisleRepository),
            GetAllProductsUseCase(productRepository),
            AddAisleProductsUseCase(aisleProductRepository),
            IsLocationNameUniqueUseCase(locationRepository)
        )

        runBlocking {
            productRepository.add(
                listOf(
                    Product(1, "Product 1", false),
                    Product(2, "Product 2", true)
                )
            )
            val id = addLocationUseCase(
                Location(
                    id = 1,
                    type = LocationType.SHOP,
                    defaultFilter = FilterType.NEEDED,
                    name = "Shop 1",
                    pinned = false,
                    aisles = emptyList()
                )
            )
            existingLocation = locationRepository.get(id)!!
        }
    }

    @AfterEach
    fun tearDown() {
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
    fun addLocation_IsExistingLocation_LocationUpdated() {
        val updateLocation =
            existingLocation.copy(
                name = existingLocation.name + " Updated",
                pinned = !existingLocation.pinned
            )
        val updatedLocation: Location?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = locationRepository.getAll().count()
            val id = addLocationUseCase(updateLocation)
            updatedLocation = locationRepository.get(id)
            countAfter = locationRepository.getAll().count()
        }
        assertNotNull(updatedLocation)
        assertEquals(countBefore, countAfter)
        assertEquals(updateLocation.id, updatedLocation?.id)
        assertEquals(updateLocation.name, updatedLocation?.name)
        assertEquals(updateLocation.type, updatedLocation?.type)
        assertEquals(updateLocation.pinned, updatedLocation?.pinned)
        assertEquals(updateLocation.defaultFilter, updatedLocation?.defaultFilter)
    }

    @Test
    fun addLocation_LocationUpdated_DoesNotAddDefaultAisle() {
        val updateLocation =
            existingLocation.copy(
                name = existingLocation.name + " Updated",
                pinned = !existingLocation.pinned
            )
        val aisleCountBefore: Int
        val aisleCountAfter: Int
        runBlocking {
            aisleCountBefore = aisleRepository.getAll().count()
            addLocationUseCase(updateLocation)
            aisleCountAfter = aisleRepository.getAll().count()
        }
        assertEquals(aisleCountBefore, aisleCountAfter)
    }

    @Test
    fun addLocation_LocationUpdated_DoesNotAddAisleProducts() {
        val updateLocation =
            existingLocation.copy(
                name = existingLocation.name + " Updated",
                pinned = !existingLocation.pinned
            )
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        runBlocking {
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            addLocationUseCase(updateLocation)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
        }
        assertEquals(aisleProductCountBefore, aisleProductCountAfter)
    }

    private fun getNewLocation(): Location {
        val newLocation = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "Shop 2",
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
            productCount = productRepository.getAll().count()
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
            defaultAisle = aisleRepository.getDefaultAisleFor(id)
            aisleProducts =
                aisleProductRepository.getAll().filter { it.aisleId == defaultAisle?.id }
        }
        assertEquals(productCount, aisleProducts.count())
        assertEquals(aisleProductCountBefore + productCount, aisleProductCountAfter)
    }
}