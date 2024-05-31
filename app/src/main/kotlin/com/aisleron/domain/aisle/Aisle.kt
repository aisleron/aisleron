package com.aisleron.domain.aisle

import com.aisleron.domain.aisleproduct.AisleProduct

data class Aisle(
    val name: String,
    val products: List<AisleProduct>,
    val locationId: Int,
    val rank: Int,
    val id: Int,
    val isDefault: Boolean
)