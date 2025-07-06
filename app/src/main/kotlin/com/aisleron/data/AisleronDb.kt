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

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.location.LocationDao
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardDao
import com.aisleron.data.maintenance.MaintenanceDao
import com.aisleron.data.product.ProductDao

interface AisleronDb {
    fun aisleDao(): AisleDao
    fun locationDao(): LocationDao
    fun productDao(): ProductDao
    fun aisleProductDao(): AisleProductDao
    fun maintenanceDao(): MaintenanceDao
    fun loyaltyCardDao(): LoyaltyCardDao
    fun locationLoyaltyCardDao(): LocationLoyaltyCardDao
}