package com.aisleron.domain.product

data class Product(
    val id: Long,
    var name: String,
    var inStock: Boolean = true
)