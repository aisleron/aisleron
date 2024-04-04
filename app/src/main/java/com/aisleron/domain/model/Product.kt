package com.aisleron.domain.model

data class Product(
    val id: Long,
    var name: String,
    var inStock : Boolean = true
)