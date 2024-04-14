package com.aisleron.domain.aisle

data class Aisle(
    val name: String,
    val products: List<AisleProduct>,
    val locationId: Int,
    var rank: Int,
    val id: Int,
    val isDefault: Boolean
)