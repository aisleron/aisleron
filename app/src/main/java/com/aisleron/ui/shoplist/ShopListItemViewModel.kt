package com.aisleron.ui.shoplist

import com.aisleron.domain.FilterType


data class ShopListItemViewModel(
    val name: String,
    val id: Int,
    val defaultFilter: FilterType
)
