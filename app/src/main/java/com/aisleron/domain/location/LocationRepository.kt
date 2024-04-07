package com.aisleron.domain.location

import com.aisleron.domain.base.BaseRepository

interface LocationRepository : BaseRepository<Location> {
    fun getByType(type: LocationType): List<Location>
    fun getShops(): List<Location>
    fun getHome(): Location
}