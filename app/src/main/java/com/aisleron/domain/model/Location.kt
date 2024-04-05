package com.aisleron.domain.model

import java.io.Serializable

data class Location(
    val id: Long,
    val type: LocationType,
    val defaultFilter: FilterType,
    var name: String,
    var pinned: Boolean,
    var aisles: List<Aisle>? = null
) : Serializable