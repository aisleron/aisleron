package com.aisleron.domain.location

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.FilterType
import java.io.Serializable

data class Location(
    val id: Int,
    val type: LocationType,
    val defaultFilter: FilterType,
    var name: String,
    var pinned: Boolean,
    var aisles: List<Aisle>? = null
) : Serializable