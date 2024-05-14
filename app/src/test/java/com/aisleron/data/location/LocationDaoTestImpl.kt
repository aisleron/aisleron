package com.aisleron.data.location

import kotlinx.coroutines.flow.Flow

class LocationDaoTestImpl : LocationDao {

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
                pinned = it.pinned
            )

            locationList.add(newEntity)
            result.add(newEntity.id.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: LocationEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun getLocation(locationId: Int): LocationEntity? {
        return locationList.find { it.id == locationId }
    }

    override suspend fun getLocations(vararg locationId: Int): List<LocationEntity> {
        return locationList.filter { it.id in locationId }
    }

    override suspend fun getLocations(): List<LocationEntity> {
        return locationList
    }

    override suspend fun getLocationByName(name: String): LocationEntity? {
        return locationList.find { it.name.uppercase() == name.uppercase() }
    }

    override suspend fun getLocationWithAisles(locationId: Int): LocationWithAisles {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationsWithAisles(vararg locationId: Int): List<LocationWithAisles> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationsWithAisles(): List<LocationWithAisles> {
        TODO("Not yet implemented")
    }

    override fun getLocationWithAislesWithProducts(locationId: Int): Flow<LocationWithAislesWithProducts?> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationsWithAislesWithProducts(vararg locationId: Int): List<LocationWithAislesWithProducts> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationsWithAislesWithProducts(): List<LocationWithAislesWithProducts> {
        TODO("Not yet implemented")
    }

    override fun getShops(): Flow<List<LocationEntity>> {
        TODO("Not yet implemented")
    }

    override fun getPinnedShops(): Flow<List<LocationEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun getHome(): LocationEntity {
        TODO("Not yet implemented")
    }
}