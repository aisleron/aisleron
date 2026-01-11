package com.aisleron.domain.shoppinglist

import com.aisleron.domain.FilterType

data class ShoppingListFilter(
    val productFilter: FilterType? = null,
    val productNameQuery: String = "",
    val showEmptyAisles: Boolean = false
)