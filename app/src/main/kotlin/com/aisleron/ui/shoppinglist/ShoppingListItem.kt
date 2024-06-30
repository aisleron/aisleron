package com.aisleron.ui.shoppinglist

interface ShoppingListItem {
    val aisleRank: Int
    val rank: Int
    val id: Int
    val name: String
    val aisleId: Int
    val itemType: ItemType

    override fun equals(other: Any?): Boolean

    enum class ItemType {
        AISLE, PRODUCT
    }
}

