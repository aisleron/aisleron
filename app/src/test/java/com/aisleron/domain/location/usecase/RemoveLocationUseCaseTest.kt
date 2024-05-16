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
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveLocationUseCaseTest {

    private lateinit var removeLocationUseCase: RemoveLocationUseCase
    private lateinit var locationRepository: LocationRepositoryImpl
    private lateinit var existingLocation: Location

    private lateinit var aisleRepository: AisleRepositoryImpl
    private lateinit var aisleProductRepository: AisleProductRepositoryImpl
    private lateinit var productRepository: ProductRepositoryImpl

    @BeforeEach
    fun setUp() {
        val aisleDao = AisleDaoTestImpl()
        locationRepository = LocationRepositoryImpl(LocationDaoTestImpl(aisleDao), LocationMapper())
        aisleRepository = AisleRepositoryImpl(aisleDao, AisleMapper())
        aisleProductRepository =
            AisleProductRepositoryImpl(AisleProductDaoTestImpl(), AisleProductRankMapper())
        productRepository =
            ProductRepositoryImpl(ProductDaoTestImpl(), AisleProductDaoTestImpl(), ProductMapper())

        removeLocationUseCase = RemoveLocationUseCase(
            locationRepository,
            RemoveAisleUseCase(
                aisleRepository,
                UpdateAisleProductsUseCase(aisleProductRepository),
                RemoveProductsFromAisleUseCase(aisleProductRepository)
            ),
            RemoveDefaultAisleUseCase(
                aisleRepository,
                RemoveProductsFromAisleUseCase(aisleProductRepository)
            )
        )

        runBlocking {
            productRepository.add(
                listOf(
                    Product(1, "Product 1", false),
                    Product(2, "Product 2", true)
                )
            )

            val addLocationUseCase = AddLocationUseCase(
                locationRepository,
                AddAisleUseCase(aisleRepository),
                GetAllProductsUseCase(productRepository),
                AddAisleProductsUseCase(aisleProductRepository),
                IsLocationNameUniqueUseCase(locationRepository)
            )

            addLocationUseCase(
                Location(
                    id = 1,
                    type = LocationType.SHOP,
                    defaultFilter = FilterType.NEEDED,
                    name = "Shop 1",
                    pinned = false,
                    aisles = emptyList()
                )
            )

            val id = addLocationUseCase(
                Location(
                    id = 2,
                    type = LocationType.SHOP,
                    defaultFilter = FilterType.NEEDED,
                    name = "Shop 2",
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
    fun removeLocation_IsExistingLocation_LocationRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedLocation: Location?
        runBlocking {
            countBefore = locationRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            removedLocation = locationRepository.get(existingLocation.id)
            countAfter = locationRepository.getAll().count()
        }
        assertNull(removedLocation)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeLocation_LocationRemoved_AislesRemoved() {
        val aisleCountBefore: Int
        val aisleCountAfter: Int
        val aisleCountLocation: Int
        runBlocking {
            aisleCountLocation = aisleRepository.getForLocation(existingLocation.id).count()
            aisleCountBefore = aisleRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            aisleCountAfter = aisleRepository.getAll().count()
        }
        assertEquals(aisleCountBefore - aisleCountLocation, aisleCountAfter)
    }

    @Test
    fun removeLocation_LocationRemoved_AisleProductsRemoved() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCountLocation: Int
        runBlocking {
            val aisles = aisleRepository.getForLocation(existingLocation.id)
            aisleProductCountLocation =
                aisleProductRepository.getAll().count { it.aisleId in aisles.map { a -> a.id } }
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            removeLocationUseCase(existingLocation)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
        }
        assertEquals(aisleProductCountBefore - aisleProductCountLocation, aisleProductCountAfter)
    }

    //Aisle Products Deleted
}