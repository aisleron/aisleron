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

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase

data class AisleShoppingListItemViewModel(
    override val rank: Int,
    override val id: Int,
    override val name: String,
    override val isDefault: Boolean,
    override var childCount: Int = 0,
    override val locationId: Int,
    override val expanded: Boolean,
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
                isDefault = isDefault,
                expanded = expanded
            )
        )
    }
}
