package com.aisleron.domain.model

data class Aisle(
    override val name: String,
    var products: List<Product>?,
    val location: Location?,
    var rank: Int,
    override val id: Long,
): ShoppingListItem()