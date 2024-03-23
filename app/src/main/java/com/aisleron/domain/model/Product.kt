package com.aisleron.domain.model

data class Product(
    val id: Int,
    var name: String,
    var inStock : Boolean = true
)