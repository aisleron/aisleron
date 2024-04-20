package com.aisleron.ui.shoppinglist

data class ShoppingListItemViewModel(
    val lineItemType: ShoppingListItemType,
    var aisleRank: Int,
    var rank: Int,
    val id: Int,
    val name: String,
    var inStock: Boolean,
    var aisleId: Int,
    var modified: Boolean = false,
    val mappingId: Int
)