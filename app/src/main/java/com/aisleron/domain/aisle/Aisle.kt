package com.aisleron.domain.aisle

import com.aisleron.domain.location.Location
import com.aisleron.domain.product.Product

data class Aisle(
    val name: String,
    val products: List<Product>,
    val location: Location?,
    var rank: Int,
    val id: Long,
)