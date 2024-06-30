package com.aisleron.ui.shoppinglist

interface ShoppingListItemViewModel {
    suspend fun remove()
    suspend fun updateRank()
}