package com.aisleron.domain.model

import java.io.Serializable

enum class LocationType {
    GENERIC, SHOP
}

enum class FilterType {
    INSTOCK, NEEDED, ALL
}

data class Location(
    val id: Int,
    val type: LocationType,
    val defaultFilter: FilterType,
    var name: String,
    var aisles: List<Aisle>? = null
) : Serializable