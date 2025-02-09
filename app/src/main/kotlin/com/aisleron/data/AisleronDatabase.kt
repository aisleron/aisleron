package com.aisleron.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.aisleproduct.AisleProductEntity
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationEntity
import com.aisleron.data.maintenance.MaintenanceDao
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductEntity

@Database(
    entities = [AisleEntity::class, LocationEntity::class, ProductEntity::class, AisleProductEntity::class],
    version = 1
)
abstract class AisleronDatabase : AisleronDb, RoomDatabase() {
    abstract override fun aisleDao(): AisleDao
    abstract override fun locationDao(): LocationDao
    abstract override fun productDao(): ProductDao
    abstract override fun aisleProductDao(): AisleProductDao
    abstract override fun maintenanceDao(): MaintenanceDao
}