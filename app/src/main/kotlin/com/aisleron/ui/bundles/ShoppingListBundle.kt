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

package com.aisleron.ui.bundles

import android.os.Parcelable
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.shoppinglist.ShoppingListGrouping
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShoppingListBundle(
    val filterType: FilterType,
    val listGrouping: ShoppingListGrouping
) : Parcelable {
    companion object {
        operator fun invoke(locationId: Int?, filterType: FilterType?): ShoppingListBundle {
            val listGrouping = if (locationId != null) {
                ShoppingListGrouping.AisleGrouping(locationId)
            } else {
                ShoppingListGrouping.LocationGrouping(LocationType.SHOP)
            }

            return ShoppingListBundle(filterType ?: FilterType.ALL, listGrouping)
        }
    }
}
