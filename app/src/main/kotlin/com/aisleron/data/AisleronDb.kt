package com.aisleron.data

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.location.LocationDao
import com.aisleron.data.maintenance.MaintenanceDao
import com.aisleron.data.product.ProductDao

interface AisleronDb {
    fun aisleDao(): AisleDao
    fun locationDao(): LocationDao
    fun productDao(): ProductDao
    fun aisleProductDao(): AisleProductDao
    fun maintenanceDao(): MaintenanceDao
}