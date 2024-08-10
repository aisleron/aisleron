package com.aisleron.data

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
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceTestImpl
import kotlinx.coroutines.runBlocking

class TestDataManager {

    private val productDao = ProductDaoTestImpl()
    private val aisleProductDao = AisleProductDaoTestImpl(productDao)
    private val aisleDao = AisleDaoTestImpl(aisleProductDao)
    private val locationDao = LocationDaoTestImpl(aisleDao)

    val productRepository = ProductRepositoryImpl(productDao, aisleProductDao, ProductMapper())
    val aisleProductRepository =
        AisleProductRepositoryImpl(aisleProductDao, AisleProductRankMapper())
    val aisleRepository = AisleRepositoryImpl(aisleDao, AisleMapper())
    val locationRepository = LocationRepositoryImpl(locationDao, LocationMapper())

    val databaseMaintenance = DatabaseMaintenanceTestImpl()

    init {
        initializeTestData()
    }

    private fun initializeTestData() {
        runBlocking {
            addProducts()
            addLocations()
            addAisles()
        }
    }

    private suspend fun addAisles() {
        locationRepository.getAll().forEach {
            aisleRepository.add(
                listOf(
                    Aisle(
                        id = 0,
                        name = "Aisle 1",
                        products = emptyList(),
                        locationId = it.id,
                        rank = 1,
                        isDefault = false
                    ),
                    Aisle(
                        id = 0,
                        name = "Aisle 2",
                        products = emptyList(),
                        locationId = it.id,
                        rank = 2,
                        isDefault = false
                    )
                )
            )
        }
    }

    private suspend fun addLocations() {
        val addLocationUseCase = AddLocationUseCaseImpl(
            locationRepository,
            AddAisleUseCaseImpl(aisleRepository, GetLocationUseCase(locationRepository)),
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

        addLocationUseCase(
            Location(
                id = 2,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = "Shop 2",
                pinned = false,
                aisles = emptyList()
            )
        )

        addLocationUseCase(
            Location(
                id = 3,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = "Shop 3",
                pinned = true,
                aisles = emptyList()
            )
        )

        addLocationUseCase(
            Location(
                id = 4,
                type = LocationType.HOME,
                defaultFilter = FilterType.NEEDED,
                name = "Home",
                pinned = false,
                aisles = emptyList()
            )
        )
    }

    private suspend fun addProducts() {
        productRepository.add(
            listOf(
                Product(1, "Product 1", false),
                Product(2, "Product 2", true),
                Product(3, "Product 3", true),
                Product(4, "Product 4", false)
            )
        )
    }
}