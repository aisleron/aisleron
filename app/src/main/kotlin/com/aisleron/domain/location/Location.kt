package com.aisleron.domain.location

import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import java.io.Serializable

data class Location(
    val id: Int,
    val type: LocationType,
    val defaultFilter: FilterType,
    val name: String,
    val pinned: Boolean,
    val aisles: List<Aisle>
) : Serializable