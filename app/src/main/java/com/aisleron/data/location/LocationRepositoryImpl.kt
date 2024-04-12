package com.aisleron.data.location

import com.aisleron.data.AisleronDatabase
import com.aisleron.data.aisle.AisleWithProductsMapper
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType

class LocationRepositoryImpl(
    private val db: AisleronDatabase,
    private val locationMapper: LocationMapper
) : LocationRepository {

    override suspend fun getByType(type: LocationType): List<Location> {
        TODO("Not yet implemented")

    }

    override suspend fun getShops(): List<Location> {
        return locationMapper.toModelList(db.locationDao().getShops())
    }

    override suspend fun getHome(): Location {
        return db.locationDao().getHome().let { locationMapper.toModel(it) }
    }

    override suspend fun getPinnedShops(): List<Location> {
        return locationMapper.toModelList(db.locationDao().getPinnedShops())
    }

    override suspend fun getLocationWithAislesWithProducts(id: Int): Location? {
        return db.locationDao().getLocationWithAislesWithProducts(id)?.toLocation()
    }

    override suspend fun get(id: Int): Location? {
        return db.locationDao().getLocation(id)?.let { locationMapper.toModel(it) }
    }

    override suspend fun getAll(): List<Location> {
        return locationMapper.toModelList(db.locationDao().getLocations())
    }

    override suspend fun add(item: Location): Int {
        return db.locationDao().upsert(locationMapper.fromModel(item))[0].toInt()
    }

    override suspend fun update(item: Location) {
        db.locationDao().upsert(locationMapper.fromModel(item))
    }

    override suspend fun remove(item: Location) {
        db.locationDao().delete(locationMapper.fromModel(item))
    }

    override suspend fun remove(id: Int) {
        TODO("Not yet implemented")
    }

    private fun LocationWithAislesWithProducts.toLocation(): Location {
        val location = locationMapper.toModel(this.location)
        location.aisles = AisleWithProductsMapper().toModelList(this.aisles, location)
        return location
    }
}




