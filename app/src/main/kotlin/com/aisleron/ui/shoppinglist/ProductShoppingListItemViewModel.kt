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

import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase

class ProductShoppingListItemViewModel(
    private val aisleProduct: AisleProduct,
    override val headerRank: Int,
    override val selected: Boolean,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val removeProductUseCase: RemoveProductUseCase,
    private val updateProductStatusUseCase: UpdateProductStatusUseCase,
    private val updateProductQtyNeededUseCase: UpdateProductQtyNeededUseCase,
    private val changeProductAisleUseCase: ChangeProductAisleUseCase
) : ProductShoppingListItem, ShoppingListItemViewModel {
    override val inStock: Boolean get() = aisleProduct.product.inStock
    override val qtyNeeded: Double get() = aisleProduct.product.qtyNeeded
    override val noteId: Int? get() = aisleProduct.product.noteId
    override val noteText: String? get() = aisleProduct.product.note?.noteText
    override val qtyIncrement: Double get() = aisleProduct.product.qtyIncrement
    override val unitOfMeasure: String get() = aisleProduct.product.unitOfMeasure
    override val trackingMode: TrackingMode get() = aisleProduct.product.trackingMode
    override val rank: Int get() = aisleProduct.rank
    override val id: Int get() = aisleProduct.product.id
    override val name: String get() = aisleProduct.product.name
    override val aisleId: Int get() = aisleProduct.aisleId

    override suspend fun remove() {
        removeProductUseCase(id)
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        updateAisleProductRankUseCase(
            aisleProduct.copy(
                rank = if (precedingItem?.itemType == itemType) precedingItem.rank + 1 else 1,
                aisleId = precedingItem?.aisleId ?: aisleId
            )
        )
    }

    suspend fun updateStatus(inStock: Boolean) {
        updateProductStatusUseCase(id, inStock)
    }

    suspend fun updateQtyNeeded(qtyNeeded: Double?) {
        qtyNeeded?.let { updateProductQtyNeededUseCase(id, it) }
    }

    suspend fun updateAisle(newAisleId: Int) {
        changeProductAisleUseCase(id, aisleId, newAisleId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductShoppingListItemViewModel) return false

        if (aisleProduct != other.aisleProduct) return false
        if (headerRank != other.headerRank) return false
        if (selected != other.selected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = aisleProduct.hashCode()
        result = 31 * result + headerRank
        result = 31 * result + selected.hashCode()
        return result
    }
}