package com.aisleron.domain.location

import com.aisleron.domain.base.BaseRepository
import kotlinx.coroutines.flow.Flow

interface LocationRepository : BaseRepository<Location> {
    //fun getByType(type: LocationType): Flow<List<Location>>
    fun getShops(): Flow<List<Location>>
    suspend fun getHome(): Location
    fun getPinnedShops(): Flow<List<Location>>
    fun getLocationWithAislesWithProducts(id: Int): Flow<Location?>
}