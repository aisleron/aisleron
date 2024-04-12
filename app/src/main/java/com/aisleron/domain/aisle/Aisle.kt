package com.aisleron.domain.aisle

import com.aisleron.domain.location.Location

data class Aisle(
    val name: String,
    val products: List<AisleProduct>,
    val location: Location?,
    var rank: Int,
    val id: Int,
)