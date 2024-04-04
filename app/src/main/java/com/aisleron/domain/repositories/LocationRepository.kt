package com.aisleron.domain.repositories

import com.aisleron.domain.model.Location
import com.aisleron.domain.model.LocationType

interface LocationRepository : BaseRepository<Location> {
    fun getByType(type: LocationType) : List<Location>
    fun getShops(): List<Location>
    fun getHome(): Location
}