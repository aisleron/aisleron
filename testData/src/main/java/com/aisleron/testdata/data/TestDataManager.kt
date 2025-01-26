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
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceTestImpl
import kotlinx.coroutines.runBlocking

class TestDataManager(private val addData: Boolean = true) {

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
            DbInitializer(locationDao, aisleDao, this).invoke()
        }
        if (addData) {
            val testUseCaseProvider = TestUseCaseProvider(this@TestDataManager)
            runBlocking { testUseCaseProvider.createSampleDataUseCase() }
        }
    }
}