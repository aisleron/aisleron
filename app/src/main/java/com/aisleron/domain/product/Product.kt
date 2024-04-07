package com.aisleron.domain.product

data class Product(
    val id: Int,
    var name: String,
    var inStock: Boolean = true
)