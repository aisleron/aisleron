package com.aisleron.model

data class Aisle(
    var products: List<Product>,
    val location: Location,
    var rank: Int
)