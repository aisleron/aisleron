package com.aisleron.domain.aisle

import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.location.Location

interface AisleRepository : BaseRepository<Aisle> {
    suspend fun getForLocation(locationId: Int): List<Aisle>
    suspend fun getForLocation(location: Location): List<Aisle>
    suspend fun getDefaultAisles(): List<Aisle>
    suspend fun getDefaultAisleFor(locationId: Int): Aisle?
    suspend fun updateAisleRank(aisle: Aisle)
    suspend fun getWithProducts(aisleId: Int): Aisle
}