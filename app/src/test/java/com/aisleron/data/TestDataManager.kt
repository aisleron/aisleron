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
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import kotlinx.coroutines.runBlocking

class TestDataManager {

    private val productDao = ProductDaoTestImpl()
    private val aisleProductDao = AisleProductDaoTestImpl(productDao)
    private val aisleDao = AisleDaoTestImpl()
    private val locationDao = LocationDaoTestImpl(aisleDao)

    val productRepository = ProductRepositoryImpl(productDao, aisleProductDao, ProductMapper())
    val aisleProductRepository =
        AisleProductRepositoryImpl(aisleProductDao, AisleProductRankMapper())
    val aisleRepository = AisleRepositoryImpl(aisleDao, AisleMapper())
    val locationRepository = LocationRepositoryImpl(locationDao, LocationMapper())

    init {
        initializeTestData()
    }

    private fun initializeTestData() {
        val addLocationUseCase = AddLocationUseCase(
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
                    Product(2, "Product 2", true),
                    Product(3, "Product 3", true),
                    Product(4, "Product 4", false)
                )
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
        }
        return

    }
}