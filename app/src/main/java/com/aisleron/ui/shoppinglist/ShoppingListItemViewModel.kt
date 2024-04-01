package com.aisleron.ui.shoppinglist

data class ShoppingListItemViewModel(
    val lineItemType: ShoppingListItemType,
    val aisleRank: Int,
    val productRank: Int,
    val id: Long,
    val name: String,
    var inStock: Boolean?
)

enum class ShoppingListItemType {
    AISLE, PRODUCT
}