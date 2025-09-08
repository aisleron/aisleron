/*
 * Copyright (C) 2025 aisleron.com
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
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.RemoveProductUseCase

data class ProductShoppingListItemViewModel(
    override val aisleRank: Int,
    override val rank: Int,
    override val id: Int,
    override val name: String,
    override val aisleId: Int,
    override val inStock: Boolean,
    override val qtyNeeded: Int,
    private val aisleProductId: Int,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val removeProductUseCase: RemoveProductUseCase,
) : ProductShoppingListItem, ShoppingListItemViewModel {

    override suspend fun remove() {
        removeProductUseCase(id)
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        updateAisleProductRankUseCase(
            AisleProduct(
                rank = if (precedingItem?.itemType == itemType) precedingItem.rank + 1 else 1,
                aisleId = precedingItem?.aisleId ?: aisleId,
                id = aisleProductId,
                product = Product(
                    id = id,
                    name = name,
                    inStock = inStock,
                    qtyNeeded = qtyNeeded
                )
            )
        )
    }
}