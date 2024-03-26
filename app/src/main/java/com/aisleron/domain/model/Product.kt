package com.aisleron.domain.model

data class Product(
    override val id: Long,
    override var name: String,
    var inStock : Boolean = true
) : ShoppingListItem()