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
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationExpandedUseCase
import com.aisleron.domain.location.usecase.UpdateLocationRankUseCase
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
    private val changeProductAisleUseCase: ChangeProductAisleUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val removeLocationUseCase: RemoveLocationUseCase,
    private val updateLocationRankUseCase: UpdateLocationRankUseCase,
    private val updateLocationExpandedUseCase: UpdateLocationExpandedUseCase
) {
    fun createProductItemViewModel(
        aisleProduct: AisleProduct,
        headerRank: Int,
        locationId: Int,
        selections: Set<ShoppingListItem.UniqueId>
    ) = ProductShoppingListItemViewModel(
        aisleProduct = aisleProduct,
        headerRank = headerRank,
        selected = isSelected(selections, ShoppingListItem.ItemType.PRODUCT, aisleProduct.id),
        locationId = locationId,
        removeProductUseCase = removeProductUseCase,
        updateAisleProductRankUseCase = updateAisleProductRankUseCase,
        updateProductStatusUseCase = updateProductStatusUseCase,
        updateProductQtyNeededUseCase = updateProductQtyNeededUseCase,
        changeProductAisleUseCase = changeProductAisleUseCase
    )

    fun createAisleItemViewModel(
        aisle: Aisle, selections: Set<ShoppingListItem.UniqueId>
    ) = AisleShoppingListItemViewModel(
        aisle = aisle,
        selected = isSelectedHeader(selections, aisle.id),
        updateAisleRankUseCase = updateAisleRankUseCase,
        getAisleUseCase = getAisleUseCase,
        removeAisleUseCase = removeAisleUseCase,
        updateAisleExpandedUseCase = updateAisleExpandedUseCase
    )

    fun createLocationItemViewModel(
        location: Location, selections: Set<ShoppingListItem.UniqueId>
    ) = LocationShoppingListItemViewModel(
        selected = isSelectedHeader(selections, location.id),
        location = location,
        getLocationUseCase = getLocationUseCase,
        removeLocationUseCase = removeLocationUseCase,
        updateLocationRankUseCase = updateLocationRankUseCase,
        updateLocationExpandedUseCase = updateLocationExpandedUseCase
    )

    private fun isSelected(
        selections: Set<ShoppingListItem.UniqueId>, itemType: ShoppingListItem.ItemType, id: Int
    ): Boolean =
        selections.contains(ShoppingListItem.UniqueId(itemType, id))

    private fun isSelectedHeader(
        selections: Set<ShoppingListItem.UniqueId>, id: Int
    ): Boolean =
        isSelected(selections, ShoppingListItem.ItemType.HEADER, id)

}