package com.aisleron.data

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.location.LocationDao
import com.aisleron.data.maintenance.MaintenanceDao
import com.aisleron.data.maintenance.MaintenanceDaoTestImpl
import com.aisleron.data.product.ProductDao
import com.aisleron.testdata.data.aisle.AisleDaoTestImpl
import com.aisleron.testdata.data.aisleproduct.AisleProductDaoTestImpl
import com.aisleron.testdata.data.location.LocationDaoTestImpl
import com.aisleron.testdata.data.product.ProductDaoTestImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class AisleronTestDatabase : AisleronDb {

    private val _productDao = ProductDaoTestImpl()
    private val _aisleProductDao = AisleProductDaoTestImpl(_productDao)
    private val _aisleDao = AisleDaoTestImpl(_aisleProductDao)
    private val _locationDao = LocationDaoTestImpl(_aisleDao)
    private val _maintenanceDao = MaintenanceDaoTestImpl()

    override fun aisleDao(): AisleDao = _aisleDao

    override fun locationDao(): LocationDao = _locationDao

    override fun productDao(): ProductDao = _productDao

    override fun aisleProductDao(): AisleProductDao = _aisleProductDao

    override fun maintenanceDao(): MaintenanceDao = _maintenanceDao

    init {
        initializeDatabase()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initializeDatabase() {
        DbInitializer(
            _locationDao, aisleDao(), TestScope(UnconfinedTestDispatcher())
        ).invoke()
    }
}