/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.ui.shoppinglist

import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase

data class ProductShoppingListItemViewModel(
    override val headerRank: Int,
    override val selected: Boolean,
    override val locationId: Int,
    override val inStock: Boolean,
    override val qtyNeeded: Double,
    override val noteId: Int?,
    override val noteText: String?,
    override val qtyIncrement: Double,
    override val unitOfMeasure: String,
    override val trackingMode: TrackingMode,
    override val rank: Int,
    override val id: Int,
    override val name: String,
    override val aisleId: Int,
    val aisleProductId: Int
) : ProductShoppingListItem, ShoppingListItemViewModel {
    lateinit var updateAisleProductRankUseCase: UpdateAisleProductRankUseCase
    lateinit var removeProductUseCase: RemoveProductUseCase
    lateinit var updateProductStatusUseCase: UpdateProductStatusUseCase
    lateinit var updateProductQtyNeededUseCase: UpdateProductQtyNeededUseCase
    lateinit var changeProductAisleUseCase: ChangeProductAisleUseCase

    override val uniqueId: ShoppingListItem.UniqueId
        get() = ShoppingListItem.UniqueId(itemType, aisleProductId)

    override suspend fun remove() {
        removeProductUseCase(id)
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        val newRank = if (precedingItem?.itemType == itemType) precedingItem.rank + 1 else 1
        val newAisleId = precedingItem?.aisleId ?: aisleId
        updateAisleProductRankUseCase(aisleProductId, newRank, newAisleId)
    }

    override fun editNavigationEvent(): ShoppingListViewModel.ShoppingListEvent =
        ShoppingListViewModel.ShoppingListEvent.NavigateToEditProduct(id)

    suspend fun updateStatus(inStock: Boolean) {
        updateProductStatusUseCase(id, inStock)
    }

    suspend fun updateQtyNeeded(qtyNeeded: Double?) {
        qtyNeeded?.let { updateProductQtyNeededUseCase(id, it) }
    }

    suspend fun updateAisle(newAisleId: Int) {
        changeProductAisleUseCase(id, aisleId, newAisleId)
    }
}