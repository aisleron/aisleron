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

package com.aisleron.ui.navigation

import androidx.annotation.IdRes
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType

interface Navigator {
    fun navigateToAddShop()
    fun navigateToEditShop(locationId: Int)
    fun navigateToAddProduct(filterType: FilterType, name: String = "", aisleId: Int? = null)
    fun navigateToEditProduct(productId: Int)
    fun navigateToAisleGroupedProductList(locationId: Int, productFilter: FilterType)
    fun navigateToLocationGroupedProductList(locationType: LocationType, productFilter: FilterType)
    fun navigateToDefaultRoute(@IdRes destinationId: Int)
    fun navigateToWelcome()
}