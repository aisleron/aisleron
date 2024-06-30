package com.aisleron.ui.shoppinglist

interface ShoppingListItem {
    val aisleRank: Int
    val rank: Int
    val id: Int
    val name: String
    val aisleId: Int
    val itemType: ItemType

    enum class ItemType {
        AISLE, PRODUCT
    }
}

