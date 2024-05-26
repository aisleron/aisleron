package com.aisleron.ui.shoppinglist

data class ShoppingListItemViewModel(
    val lineItemType: ShoppingListItemType,
    var aisleRank: Int,
    var rank: Int,
    val id: Int,
    val name: String,
    val inStock: Boolean, //Product: inStock; Aisle: isDefault
    var aisleId: Int,
    val mappingId: Int, //Product: ProductAisle Id, Aisle: 0
    val childCount: Int //Product: 0, Aisle: Count of Products
)