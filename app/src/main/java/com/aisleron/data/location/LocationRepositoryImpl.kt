package com.aisleron.data.location

import com.aisleron.data.AisleronDatabase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType

class LocationRepositoryImpl(private val db: AisleronDatabase) : LocationRepository {

    override fun getByType(type: LocationType): List<Location> {
        TODO("Not yet implemented")

    }

    override suspend fun getShops(): List<Location> {
        return db.locationDao().getShops().map { it.toLocation() }
    }

    override fun getHome(): Location {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: Int): Location? {
        return db.locationDao().getLocation(id)?.toLocation()
        //return LocationData.locations.find { l -> l.id == id }
    }

    override suspend fun getAll(): List<Location> {
        TODO("Not yet implemented")
    }

    override suspend fun add(item: Location): Int {
        return db.locationDao().upsert(item.toLocationEntity())[0].toInt()
    }

    override suspend fun update(item: Location) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(item: Location) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(id: Int) {
        TODO("Not yet implemented")
    }

    private fun LocationEntity.toLocation() = Location(
        id = id,
        defaultFilter = defaultFilter,
        name = name,
        type = type,
        pinned = pinned
    )

    private fun Location.toLocationEntity() = LocationEntity(
        id = id,
        defaultFilter = defaultFilter,
        name = name,
        type = type,
        pinned = pinned
    )
}