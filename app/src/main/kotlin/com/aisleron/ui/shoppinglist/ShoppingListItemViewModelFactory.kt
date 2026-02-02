/*
 * Copyright (C) 2026 aisleron.com
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

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase

class ShoppingListItemViewModelFactory(
    private val updateAisleRankUseCase: UpdateAisleRankUseCase,
    private val getAisleUseCase: GetAisleUseCase,
    private val removeAisleUseCase: RemoveAisleUseCase,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val updateAisleExpandedUseCase: UpdateAisleExpandedUseCase,
    private val removeProductUseCase: RemoveProductUseCase,
    private val updateProductStatusUseCase: UpdateProductStatusUseCase,
    private val updateProductQtyNeededUseCase: UpdateProductQtyNeededUseCase,
    private val changeProductAisleUseCase: ChangeProductAisleUseCase
) {
    fun createProductItemViewModel(
        aisleProduct: AisleProduct, aisleRank: Int, isSelected: Boolean
    ) = ProductShoppingListItemViewModel(
        aisleProduct = aisleProduct,
        aisleRank = aisleRank,
        selected = isSelected,
        removeProductUseCase = removeProductUseCase,
        updateAisleProductRankUseCase = updateAisleProductRankUseCase,
        updateProductStatusUseCase = updateProductStatusUseCase,
        updateProductQtyNeededUseCase = updateProductQtyNeededUseCase,
        changeProductAisleUseCase = changeProductAisleUseCase
    )

    fun createAisleItemViewModel(
        aisle: Aisle, isSelected: Boolean
    ) = AisleShoppingListItemViewModel(
        aisle = aisle,
        selected = isSelected,
        updateAisleRankUseCase = updateAisleRankUseCase,
        getAisleUseCase = getAisleUseCase,
        removeAisleUseCase = removeAisleUseCase,
        updateAisleExpandedUseCase = updateAisleExpandedUseCase
    )
}