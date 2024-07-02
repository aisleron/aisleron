package com.aisleron.ui.shoppinglist

interface ProductShoppingListItem : ShoppingListItem {
    val inStock: Boolean

    override val itemType: ShoppingListItem.ItemType
        get() = ShoppingListItem.ItemType.PRODUCT
}