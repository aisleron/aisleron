package com.aisleron.data.location

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepositoryImpl(
    private val locationDao: LocationDao,
    private val locationMapper: LocationMapper
) : LocationRepository {

    /*
    override fun getByType(type: LocationType): Flow<List<Location>> {
        return when (type) {
            LocationType.HOME -> flowOf<List<Location>>(listOf(getHome()))
            LocationType.SHOP -> getShops()
        }
    }

     */

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

    override suspend fun getLocationWithAisles(id: Int): Location {
        return LocationWithAislesMapper().toModel(locationDao.getLocationWithAisles(id))
    }

    override suspend fun get(id: Int): Location? {
        return locationDao.getLocation(id)?.let { locationMapper.toModel(it) }
    }

    override suspend fun getMultiple(vararg id: Int): List<Location> {
        // '*' is a spread operator required to pass vararg down
        return locationMapper.toModelList(locationDao.getLocations(*id))
    }

    override suspend fun getAll(): List<Location> {
        return locationMapper.toModelList(locationDao.getLocations())
    }

    override suspend fun add(item: Location): Int {
        return locationDao.upsert(locationMapper.fromModel(item))[0].toInt()
    }

    override suspend fun add(items: List<Location>): List<Int> {
        return upsertLocations(items)
    }

    override suspend fun update(item: Location) {
        locationDao.upsert(locationMapper.fromModel(item))
    }

    override suspend fun update(items: List<Location>) {
        upsertLocations(items)
    }

    private suspend fun upsertLocations(locations: List<Location>): List<Int> {
        return locationDao
            .upsert(*locationMapper.fromModelList(locations).map { it }.toTypedArray())
            .map { it.toInt() }
    }

    override suspend fun remove(item: Location) {
        locationDao.delete(locationMapper.fromModel(item))
    }
}




