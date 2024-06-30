package com.aisleron.ui.shoppinglist

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase

data class AisleShoppingListItem(
    override val rank: Int,
    override val id: Int,
    override val name: String,
    val isDefault: Boolean,
    val childCount: Int,
    val locationId: Int,
    private val updateAisleRankUseCase: UpdateAisleRankUseCase,
    private val getAisleUseCase: GetAisleUseCase,
    private val removeAisleUseCase: RemoveAisleUseCase
) : ShoppingListItem, ShoppingListItemViewModel {
    override val aisleId: Int
        get() = id
    override val lineItemType: ShoppingListItemType
        get() = ShoppingListItemType.AISLE
    override val aisleRank: Int
        get() = rank

    override suspend fun remove() {
        val aisle = getAisleUseCase(id)
        aisle?.let { removeAisleUseCase(it) }
    }

    override suspend fun updateRank() {
        updateAisleRankUseCase(
            Aisle(
                id = id,
                name = name,
                products = emptyList(),
                locationId = locationId,
                rank = rank,
                isDefault = isDefault
            )
        )
    }
}
