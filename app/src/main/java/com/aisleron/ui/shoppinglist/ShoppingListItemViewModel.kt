package com.aisleron.ui.shoppinglist

import com.aisleron.domain.model.ShoppingListItem

data class ShoppingListItemViewModel(
    val lineItemType: ShoppingListItemType,
    val aisleRank: Int,
    val productRank: Int,
    val item: ShoppingListItem
)

enum class ShoppingListItemType {
    AISLE, PRODUCT
}