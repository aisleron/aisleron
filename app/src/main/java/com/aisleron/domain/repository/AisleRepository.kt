package com.aisleron.domain.repository

import com.aisleron.domain.model.Aisle
import com.aisleron.domain.model.Location

interface AisleRepository : BaseRepository<Aisle> {
    fun getForLocation(locationId: Long): List<Aisle>
    fun getForLocation(location: Location): List<Aisle>
}