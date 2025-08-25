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

package com.aisleron.ui.bundles

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.copyentity.CopyEntityType

class Bundler {

    private fun <T> getParcelableBundle(bundle: Bundle?, key: String, clazz: Class<T>): T? {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelable(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            bundle?.getParcelable(key) as T?
        }
        return result
    }

    private fun makeParcelableBundle(key: String, value: Parcelable): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(key, value)
        return bundle
    }

    fun makeEditProductBundle(productId: Int, locationId: Int? = null): Bundle {
        val editProductBundle = AddEditProductBundle(
            productId = productId,
            actionType = AddEditProductBundle.ProductAction.EDIT,
            aisleId = null,
            locationId = locationId
        )
        return makeParcelableBundle(ADD_EDIT_PRODUCT, editProductBundle)
    }

    fun makeAddProductBundle(
        name: String? = null,
        inStock: Boolean = false,
        aisleId: Int? = null,
        locationId: Int? = null
    ): Bundle {
        val addProductBundle = AddEditProductBundle(
            name = name,
            inStock = inStock,
            actionType = AddEditProductBundle.ProductAction.ADD,
            aisleId = aisleId,
            locationId = locationId

        )
        return makeParcelableBundle(ADD_EDIT_PRODUCT, addProductBundle)
    }

    fun getAddEditProductBundle(bundle: Bundle?): AddEditProductBundle {
        val result = getParcelableBundle(bundle, ADD_EDIT_PRODUCT, AddEditProductBundle::class.java)
        return result ?: AddEditProductBundle()
    }

    fun makeEditLocationBundle(locationId: Int): Bundle {
        val editLocationBundle = AddEditLocationBundle(
            locationId = locationId,
            actionType = AddEditLocationBundle.LocationAction.EDIT,
            locationType = LocationType.SHOP
        )
        return makeParcelableBundle(ADD_EDIT_LOCATION, editLocationBundle)
    }

    fun makeAddLocationBundle(name: String? = null): Bundle {
        val addLocationBundle = AddEditLocationBundle(
            name = name,
            locationType = LocationType.SHOP,
            actionType = AddEditLocationBundle.LocationAction.ADD
        )
        return makeParcelableBundle(ADD_EDIT_LOCATION, addLocationBundle)
    }

    fun getAddEditLocationBundle(bundle: Bundle?): AddEditLocationBundle {
        val result =
            getParcelableBundle(bundle, ADD_EDIT_LOCATION, AddEditLocationBundle::class.java)
        return result ?: AddEditLocationBundle()
    }

    fun makeShoppingListBundle(locationId: Int, filterType: FilterType): Bundle {
        val shoppingListBundle = ShoppingListBundle(
            locationId = locationId,
            filterType = filterType
        )
        return makeParcelableBundle(SHOPPING_LIST_BUNDLE, shoppingListBundle)
    }

    fun getShoppingListBundle(bundle: Bundle?): ShoppingListBundle {
        var result: ShoppingListBundle? =
            getParcelableBundle(bundle, SHOPPING_LIST_BUNDLE, ShoppingListBundle::class.java)
        if (result == null) {
            val locationId = bundle?.getInt(ARG_LOCATION_ID, 1)
            val filterType =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle?.getSerializable(ARG_FILTER_TYPE, FilterType::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    bundle?.getSerializable(ARG_FILTER_TYPE) as FilterType?
                }
            result = ShoppingListBundle(locationId, filterType)
        }
        return result
    }

    fun makeCopyEntityBundle(
        type: CopyEntityType, title: String, defaultName: String, nameHint: String
    ): Bundle {
        val copyEntityBundle = CopyEntityBundle(
            type = type,
            title = title,
            defaultName = defaultName,
            nameHint = nameHint
        )
        return makeParcelableBundle(COPY_ENTITY, copyEntityBundle)
    }

    fun getCopyEntityBundle(bundle: Bundle?): CopyEntityBundle {
        val result =
            getParcelableBundle(bundle, COPY_ENTITY, CopyEntityBundle::class.java)
        return result ?: CopyEntityBundle(CopyEntityType.Location(-1))

    }

    private companion object BundleType {
        const val ADD_EDIT_PRODUCT = "addEditProduct"
        const val ADD_EDIT_LOCATION = "addEditLocation"
        const val SHOPPING_LIST_BUNDLE = "shoppingList"
        const val COPY_ENTITY = "copyEntity"

        const val ARG_LOCATION_ID = "locationId"
        const val ARG_FILTER_TYPE = "filterType"
    }
}