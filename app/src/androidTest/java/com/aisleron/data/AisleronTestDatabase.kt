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
import com.aisleron.data.maintenance.MaintenanceDaoTestImpl
import com.aisleron.data.product.ProductDao
import com.aisleron.testdata.data.aisle.AisleDaoTestImpl
import com.aisleron.testdata.data.aisleproduct.AisleProductDaoTestImpl
import com.aisleron.testdata.data.location.LocationDaoTestImpl
import com.aisleron.testdata.data.loyaltycard.LocationLoyaltyCardDaoTestImpl
import com.aisleron.testdata.data.loyaltycard.LoyaltyCardDaoTestImpl
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
    private val _locationLoyaltyCardDao = LocationLoyaltyCardDaoTestImpl()
    private val _loyaltyCardDao = LoyaltyCardDaoTestImpl(_locationLoyaltyCardDao)

    override fun aisleDao(): AisleDao = _aisleDao

    override fun locationDao(): LocationDao = _locationDao

    override fun productDao(): ProductDao = _productDao

    override fun aisleProductDao(): AisleProductDao = _aisleProductDao

    override fun maintenanceDao(): MaintenanceDao = _maintenanceDao

    override fun loyaltyCardDao(): LoyaltyCardDao = _loyaltyCardDao

    override fun locationLoyaltyCardDao(): LocationLoyaltyCardDao = _locationLoyaltyCardDao

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