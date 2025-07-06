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

package com.aisleron.testdata.data.location

import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationEntity
import com.aisleron.data.location.LocationWithAisles
import com.aisleron.data.location.LocationWithAislesWithProducts
import com.aisleron.domain.location.LocationType
import com.aisleron.testdata.data.aisle.AisleDaoTestImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

class LocationDaoTestImpl(private val aisleDao: AisleDaoTestImpl) : LocationDao {

    private val locationList = mutableListOf<LocationEntity>()

    override suspend fun upsert(vararg entity: LocationEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val id: Int
            val existingEntity = getLocation(it.id)
            if (existingEntity == null) {
                id = (locationList.maxOfOrNull { e -> e.id }?.toInt() ?: 0) + 1
            } else {
                id = existingEntity.id
                locationList.removeAt(locationList.indexOf(existingEntity))
            }

            val newEntity = LocationEntity(
                id = id,
                type = it.type,
                defaultFilter = it.defaultFilter,
                name = it.name,
                pinned = it.pinned,
                showDefaultAisle = it.showDefaultAisle
            )

            locationList.add(newEntity)
            result.add(newEntity.id.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: LocationEntity) {
        locationList.removeIf { it in entity }
    }

    override suspend fun getLocation(locationId: Int): LocationEntity? {
        return locationList.find { it.id == locationId }
    }

    override suspend fun getLocations(): List<LocationEntity> {
        return locationList
    }

    override suspend fun getLocationByName(name: String): LocationEntity? {
        return locationList.find { it.name.uppercase() == name.uppercase() }
    }

    override suspend fun getLocationWithAisles(locationId: Int): LocationWithAisles {
        return LocationWithAisles(
            location = getLocation(locationId)!!,
            aisles = aisleDao.getAislesForLocation(locationId)
        )
    }

    override fun getLocationWithAislesWithProducts(locationId: Int): Flow<LocationWithAislesWithProducts?> {
        val location = locationList.firstOrNull { it.id == locationId }

        var result: LocationWithAislesWithProducts? = null

        location?.let {
            result = LocationWithAislesWithProducts(
                location = location,
                aisles = runBlocking {
                    aisleDao.getAislesWithProducts().filter { it.aisle.locationId == locationId }
                }
            )
        }
        return flowOf(result)
    }

    override fun getShops(): Flow<List<LocationEntity>> {
        return flowOf(locationList.filter { it.type == LocationType.SHOP })
    }

    override fun getPinnedShops(): Flow<List<LocationEntity>> {
        return flowOf(locationList.filter { it.type == LocationType.SHOP && it.pinned })
    }

    override suspend fun getHome(): LocationEntity {
        return locationList.first { it.type == LocationType.HOME }
    }
}