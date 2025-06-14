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
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationEntity
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DbInitializer(
    private val locationDao: LocationDao,
    private val aisleDao: AisleDao,
    coroutineScopeProvider: CoroutineScope? = null
) {
    private val coroutineScope = coroutineScopeProvider ?: CoroutineScope(Dispatchers.IO)

    operator fun invoke() {

        val home = LocationEntity(
            id = 0,
            type = LocationType.HOME,
            defaultFilter = FilterType.NEEDED,
            name = "Home",
            pinned = false,
            showDefaultAisle = true
        )
        coroutineScope.launch {
            val homeId = locationDao.upsert(home)[0].toInt()
            val aisle = AisleEntity(
                id = 0,
                name = "No Aisle",
                locationId = homeId,
                rank = 1000,
                isDefault = true,
                expanded = true
            )

            aisleDao.upsert(aisle)
        }

    }
}