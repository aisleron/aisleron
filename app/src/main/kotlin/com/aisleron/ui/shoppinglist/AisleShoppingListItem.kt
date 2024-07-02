package com.aisleron.ui.shoppinglist

interface AisleShoppingListItem : ShoppingListItem {
    val isDefault: Boolean
    val childCount: Int
    val locationId: Int
    override val aisleId: Int
        get() = id
    override val itemType: ShoppingListItem.ItemType
        get() = ShoppingListItem.ItemType.AISLE
    override val aisleRank: Int
        get() = rank
}
