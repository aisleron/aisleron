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
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationExpandedUseCase
import com.aisleron.domain.location.usecase.UpdateLocationRankUseCase
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase

class ShoppingListItemViewModelFactory(
    private val updateAisleRankUseCase: UpdateAisleRankUseCase,
    private val removeAisleUseCase: RemoveAisleUseCase,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val updateAisleExpandedUseCase: UpdateAisleExpandedUseCase,
    private val removeProductUseCase: RemoveProductUseCase,
    private val updateProductStatusUseCase: UpdateProductStatusUseCase,
    private val updateProductQtyNeededUseCase: UpdateProductQtyNeededUseCase,
    private val changeProductAisleUseCase: ChangeProductAisleUseCase,
    private val removeLocationUseCase: RemoveLocationUseCase,
    private val updateLocationRankUseCase: UpdateLocationRankUseCase,
    private val updateLocationExpandedUseCase: UpdateLocationExpandedUseCase,
    private val getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase
) {
    fun createProductItemViewModel(
        aisleProduct: AisleProduct,
        headerRank: Int,
        locationId: Int,
        selections: Set<ShoppingListItem.UniqueId>
    ) = ProductShoppingListItemViewModel(
        headerRank = headerRank,
        selected = isSelected(selections, ShoppingListItem.ItemType.PRODUCT, aisleProduct.id),
        locationId = locationId,
        inStock = aisleProduct.product.inStock,
        qtyNeeded = aisleProduct.product.qtyNeeded,
        noteId = aisleProduct.product.noteId,
        noteText = aisleProduct.product.note?.noteText,
        qtyIncrement = aisleProduct.product.qtyIncrement,
        unitOfMeasure = aisleProduct.product.unitOfMeasure,
        trackingMode = aisleProduct.product.trackingMode,
        rank = aisleProduct.rank,
        id = aisleProduct.product.id,
        name = aisleProduct.product.name,
        aisleId = aisleProduct.aisleId,
        aisleProductId = aisleProduct.id
    ).apply {
        removeProductUseCase = this@ShoppingListItemViewModelFactory.removeProductUseCase
        updateAisleProductRankUseCase =
            this@ShoppingListItemViewModelFactory.updateAisleProductRankUseCase

        updateProductStatusUseCase =
            this@ShoppingListItemViewModelFactory.updateProductStatusUseCase

        updateProductQtyNeededUseCase =
            this@ShoppingListItemViewModelFactory.updateProductQtyNeededUseCase

        changeProductAisleUseCase = this@ShoppingListItemViewModelFactory.changeProductAisleUseCase
    }

    fun createAisleItemViewModel(
        aisle: Aisle, selections: Set<ShoppingListItem.UniqueId>
    ) = AisleShoppingListItemViewModel(
        selected = isSelectedHeader(selections, aisle.id),
        childCount = aisle.products.count(),
        locationId = aisle.locationId,
        isDefault = aisle.isDefault,
        expanded = aisle.expanded,
        rank = aisle.rank,
        id = aisle.id,
        name = aisle.name
    ).apply {
        updateAisleRankUseCase = this@ShoppingListItemViewModelFactory.updateAisleRankUseCase
        removeAisleUseCase = this@ShoppingListItemViewModelFactory.removeAisleUseCase
        updateAisleExpandedUseCase =
            this@ShoppingListItemViewModelFactory.updateAisleExpandedUseCase
    }

    suspend fun createLocationItemViewModel(
        location: Location, selections: Set<ShoppingListItem.UniqueId>
    ) = LocationShoppingListItemViewModel(
        selected = isSelectedHeader(selections, location.id),
        childCount = location.aisles.sumOf { it.products.size },
        expanded = location.expanded,
        rank = location.rank,
        id = location.id,
        name = location.name,
        aisleId = location.aisles.firstOrNull { !it.isDefault }?.id ?: 0,
        showLoyaltyCard = getLoyaltyCardForLocationUseCase(location.id) != null
    ).apply {
        removeLocationUseCase = this@ShoppingListItemViewModelFactory.removeLocationUseCase
        updateLocationRankUseCase = this@ShoppingListItemViewModelFactory.updateLocationRankUseCase
        updateLocationExpandedUseCase =
            this@ShoppingListItemViewModelFactory.updateLocationExpandedUseCase

        getLoyaltyCardForLocationUseCase =
            this@ShoppingListItemViewModelFactory.getLoyaltyCardForLocationUseCase
    }

    private fun isSelected(
        selections: Set<ShoppingListItem.UniqueId>, itemType: ShoppingListItem.ItemType, id: Int
    ): Boolean =
        selections.contains(ShoppingListItem.UniqueId(itemType, id))

    private fun isSelectedHeader(
        selections: Set<ShoppingListItem.UniqueId>, id: Int
    ): Boolean =
        isSelected(selections, ShoppingListItem.ItemType.HEADER, id)

}