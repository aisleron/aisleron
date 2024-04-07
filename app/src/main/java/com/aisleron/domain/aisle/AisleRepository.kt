package com.aisleron.domain.aisle

import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.location.Location

interface AisleRepository : BaseRepository<Aisle> {
    fun getForLocation(locationId: Long): List<Aisle>
    fun getForLocation(location: Location): List<Aisle>
}