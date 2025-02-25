/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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