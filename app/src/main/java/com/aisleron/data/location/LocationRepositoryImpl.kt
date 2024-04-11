package com.aisleron.data.location

import com.aisleron.data.AisleronDatabase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType

class LocationRepositoryImpl(private val db: AisleronDatabase) : LocationRepository {

    override suspend fun getByType(type: LocationType): List<Location> {
        TODO("Not yet implemented")

    }

    override suspend fun getShops(): List<Location> {
        return db.locationDao().getShops().map { it.toLocation() }
    }

    override suspend fun getHome(): Location {
        TODO("Not yet implemented")
    }

    override suspend fun getPinnedShops(): List<Location> {
        return db.locationDao().getPinnedShops().map { it.toLocation() }
    }

    override suspend fun get(id: Int): Location? {
        return db.locationDao().getLocation(id)?.toLocation()
    }

    override suspend fun getAll(): List<Location> {
        TODO("Not yet implemented")
    }

    override suspend fun add(item: Location): Int {
        return db.locationDao().upsert(item.toLocationEntity())[0].toInt()
    }

    override suspend fun update(item: Location) {
        db.locationDao().upsert(item.toLocationEntity())
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