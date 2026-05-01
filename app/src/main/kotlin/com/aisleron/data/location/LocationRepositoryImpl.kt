/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.data.location

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepositoryImpl(
    private val locationDao: LocationDao,
    private val locationMapper: LocationMapper
) : LocationRepository {
    override fun getShops(): Flow<List<Location>> {
        val locationEntities = locationDao.getShops()
        return locationEntities.map { locationMapper.toModelList(it) }
    }

    override suspend fun getHome(): Location {
        return locationDao.getHome().let { locationMapper.toModel(it) }
    }

    override fun getPinnedShops(): Flow<List<Location>> {
        val locationEntities = locationDao.getPinnedShops()
        return locationEntities.map { locationMapper.toModelList(it) }
    }

    override fun getLocationWithAislesWithProducts(id: Int): Flow<Location?> {
        val locationEntity = locationDao.getLocationWithAislesWithProducts(id)
        return locationEntity.map { it?.let { LocationWithAislesWithProductsMapper().toModel(it) } }
    }

    override fun getLocationsWithAislesWithProducts(type: LocationType): Flow<List<Location>> {
        val locationEntity = locationDao.getLocationsWithAislesWithProducts(type)
        return locationEntity.map { LocationWithAislesWithProductsMapper().toModelList(it) }
    }

    override suspend fun getLocationWithAisles(id: Int): Location {
        return LocationWithAislesMapper().toModel(locationDao.getLocationWithAisles(id))
    }

    override suspend fun getByName(name: String): Location? {
        return locationDao.getLocationByName(name.trim())?.let { locationMapper.toModel(it) }
    }

    override suspend fun getByType(locationType: LocationType): List<Location> {
        return locationMapper.toModelList(locationDao.getByType(locationType))
    }

    override suspend fun getMaxRank(): Int {
        return locationDao.getMaxRank()
    }

    private suspend fun mapExisting(
        item: Location, includeDeleted: Boolean
    ): LocationEntity {
        val currentEntity = locationDao.getLocation(item.id, includeDeleted)
        return locationMapper.fromModel(item, currentEntity)
    }

    override suspend fun updateLocationRank(location: Location) {
        locationDao.updateRank(mapExisting(location, false))
    }

    override suspend fun get(id: Int): Location? =
        getLocation(id, false)

    override suspend fun getAll(): List<Location> {
        return locationMapper.toModelList(locationDao.getLocations())
    }

    override suspend fun add(item: Location): Int {
        return locationDao.upsert(locationMapper.fromModel(item, null)).first().toInt()
    }

    override suspend fun add(items: List<Location>): List<Int> {
        val locations = items.map { locationMapper.fromModel(it, null) }
        return upsertLocations(locations)
    }

    override suspend fun update(item: Location) {
        locationDao.upsert(mapExisting(item, false))
    }

    override suspend fun update(items: List<Location>) {
        val locations = items.map { mapExisting(it, false) }
        upsertLocations(locations)
    }

    private suspend fun upsertLocations(locations: List<LocationEntity>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return locationDao
            .upsert(*locations.toTypedArray())
            .map { it.toInt() }
    }

    override suspend fun remove(item: Location) {
        val removeEntity = mapExisting(item, false).copy(isRemoved = true)
        locationDao.upsert(removeEntity)
    }

    private suspend fun getLocation(id: Int, includeDeleted: Boolean): Location? {
        return locationDao.getLocation(id, includeDeleted)?.let { locationMapper.toModel(it) }
    }

    override suspend fun getRemoved(id: Int): Location? =
        getLocation(id, true)

    override suspend fun restore(id: Int) {
        val removedEntity = locationDao.getLocation(id, true) ?: return

        val location = locationMapper.toModel(removedEntity)
        val restoreEntity = locationMapper.fromModel(
            location, removedEntity
        ).copy(isRemoved = false)

        locationDao.upsert(restoreEntity)
    }

    override suspend fun hardDelete(item: Location) {
        locationDao.delete(mapExisting(item, true))
    }
}




