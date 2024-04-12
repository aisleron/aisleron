package com.aisleron.domain.location

import com.aisleron.domain.base.BaseRepository

interface LocationRepository : BaseRepository<Location> {
    suspend fun getByType(type: LocationType): List<Location>
    suspend fun getShops(): List<Location>
    suspend fun getHome(): Location
    suspend fun getPinnedShops(): List<Location>
    suspend fun getLocationWithAislesWithProducts(id: Int): Location?
}