package com.aisleron.ui.shoppinglist

data class ShoppingListItemViewModel(
    val lineItemType: ShoppingListItemType,
    val aisleRank: Int,
    val productRank: Int,
    val id: Int,
    val name: String,
    var inStock: Boolean?
)