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
import androidx.annotation.IdRes
import androidx.navigation.NavController
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.shoppinglist.ShoppingListGrouping
import java.lang.ref.WeakReference

class NavigatorImpl(private val bundler: Bundler) : Navigator {
    private var navControllerRef: WeakReference<NavController>? = null
    private val navController: NavController? get() = navControllerRef?.get()

    private fun navigate(@IdRes destinationId: Int, bundle: Bundle? = null) {
        navController?.navigate(destinationId, bundle)
    }

    fun attach(controller: NavController) {
        navControllerRef = WeakReference(controller)
    }

    override fun navigateToAddShop() {
        val bundle = bundler.makeAddLocationBundle()
        navigate(R.id.nav_add_shop, bundle)
    }

    override fun navigateToEditShop(locationId: Int) {
        val bundle = bundler.makeEditLocationBundle(locationId)
        navigate(R.id.nav_add_shop, bundle)
    }

    override fun navigateToAddProduct(
        filterType: FilterType, name: String, aisleId: Int?
    ) {
        val bundle = bundler.makeAddProductBundle(
            name = name,
            inStock = filterType == FilterType.IN_STOCK,
            aisleId = aisleId
        )

        navigate(R.id.nav_add_product, bundle)
    }

    override fun navigateToEditProduct(productId: Int) {
        val bundle = bundler.makeEditProductBundle(productId)
        navigate(R.id.nav_add_product, bundle)
    }

    override fun navigateToAisleGroupedProductList(locationId: Int, productFilter: FilterType) {
        val bundle = bundler.makeShoppingListBundle(
            productFilter, ShoppingListGrouping.AisleGrouping(locationId)
        )

        navigate(R.id.nav_shopping_list, bundle)
    }

    override fun navigateToLocationGroupedProductList(
        locationType: LocationType, productFilter: FilterType
    ) {
        val bundle = bundler.makeShoppingListBundle(
            productFilter, ShoppingListGrouping.LocationGrouping(locationType)
        )

        navigate(R.id.nav_shopping_list, bundle)
    }

    override fun navigateToDefaultRoute(@IdRes destinationId: Int) {
        navigate(destinationId, null)
    }

    override fun navigateToWelcome() {
        navigate(R.id.nav_welcome)
    }
}