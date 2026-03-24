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

import android.os.Bundle
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.shoppinglist.ShoppingListGrouping

class NavigatorTestImpl(private val bundler: Bundler) : Navigator {
    private var _bundle: Bundle? = null
    val bundle: Bundle? get() = _bundle

    private var _destination: Int? = null
    val destination: Int? get() = _destination

    override fun navigateToAddShop() {
        _bundle = bundler.makeAddLocationBundle()
        _destination = R.id.nav_add_shop

    }

    override fun navigateToEditShop(locationId: Int) {
        _bundle = bundler.makeEditLocationBundle(locationId)
        _destination = R.id.nav_add_shop
    }

    override fun navigateToAddProduct(
        filterType: FilterType, name: String, aisleId: Int?
    ) {
        _bundle = bundler.makeAddProductBundle(
            name = name,
            inStock = filterType == FilterType.IN_STOCK,
            aisleId = aisleId
        )

        _destination = R.id.nav_add_product
    }

    override fun navigateToEditProduct(productId: Int) {
        _bundle = bundler.makeEditProductBundle(productId)
        _destination = R.id.nav_add_product
    }

    override fun navigateToAisleGroupedProductList(
        locationId: Int, productFilter: FilterType
    ) {
        _bundle = bundler.makeShoppingListBundle(
            productFilter, ShoppingListGrouping.AisleGrouping(locationId)
        )

        _destination = R.id.nav_shopping_list
    }

    override fun navigateToLocationGroupedProductList(
        locationType: LocationType, productFilter: FilterType
    ) {
        _bundle = bundler.makeShoppingListBundle(
            productFilter, ShoppingListGrouping.LocationGrouping(locationType)
        )

        _destination = R.id.nav_shopping_list
    }

    override fun navigateToDefaultRoute(destinationId: Int) {
        _bundle = null
        _destination = destinationId
    }

    override fun navigateToWelcome() {
        _bundle = null
        _destination = R.id.nav_welcome
    }
}