package com.aisleron.ui.shoppinglist

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase

data class AisleShoppingListItemViewModel(
    override val rank: Int,
    override val id: Int,
    override val name: String,
    override val isDefault: Boolean,
    override val childCount: Int,
    override val locationId: Int,
    private val updateAisleRankUseCase: UpdateAisleRankUseCase,
    private val getAisleUseCase: GetAisleUseCase,
    private val removeAisleUseCase: RemoveAisleUseCase
) : AisleShoppingListItem, ShoppingListItemViewModel {

    override suspend fun remove() {
        val aisle = getAisleUseCase(id)
        aisle?.let { removeAisleUseCase(it) }
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        updateAisleRankUseCase(
            Aisle(
                id = id,
                name = name,
                products = emptyList(),
                locationId = locationId,
                rank = precedingItem?.let { it.aisleRank + 1 } ?: 1,
                isDefault = isDefault
            )
        )
    }
}
