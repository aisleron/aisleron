package com.aisleron.domain.model

data class Aisle(
    val name: String,
    var products: List<Product>?,
    val location: Location?,
    var rank: Int,
    val id: Int
)