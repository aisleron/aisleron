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
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test


class BundlerTest {
    private lateinit var bundler: Bundler

    private fun <T> getParcelableBundle(bundle: Bundle?, key: String, clazz: Class<T>): T? {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelable(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            bundle?.getParcelable(key) as T?
        }
        return result
    }

    @Before
    fun setUp() {
        bundler = Bundler()
    }

    @Test
    fun testMakeEditProductBundle_ProductIdProvided_EditObjectBundled() {
        val productId = 1
        val bundle = bundler.makeEditProductBundle(productId)

        val editProductBundle =
            getParcelableBundle(bundle, "addEditProduct", AddEditProductBundle::class.java)

        assertNotNull(editProductBundle)
        assertEquals(1, editProductBundle?.productId)
        assertEquals(AddEditProductBundle.ProductAction.EDIT, editProductBundle?.actionType)
    }

    @Test
    fun testMakeAddProductBundle_NoAttributesProvided_AddObjectBundled() {
        val bundle = bundler.makeAddProductBundle()

        val addProductBundle =
            getParcelableBundle(bundle, "addEditProduct", AddEditProductBundle::class.java)

        assertNotNull(addProductBundle)
        assertNull(addProductBundle?.name)
        assertFalse(addProductBundle?.inStock!!)
        assertEquals(AddEditProductBundle.ProductAction.ADD, addProductBundle.actionType)
        assertNull(addProductBundle.aisleId)
    }

    @Test
    fun testMakeAddProductBundle_NameProvided_AddObjectBundleHasName() {
        val productName = "Product Name"
        val bundle = bundler.makeAddProductBundle(productName)

        val addProductBundle =
            getParcelableBundle(bundle, "addEditProduct", AddEditProductBundle::class.java)

        assertEquals(productName, addProductBundle?.name)
        assertEquals(AddEditProductBundle.ProductAction.ADD, addProductBundle?.actionType)
    }

    @Test
    fun testMakeAddProductBundle_InStockTrue_AddObjectBundleIsInStock() {
        val inStock = true
        val bundle = bundler.makeAddProductBundle(inStock = inStock)

        val addProductBundle =
            getParcelableBundle(bundle, "addEditProduct", AddEditProductBundle::class.java)

        assertEquals(inStock, addProductBundle?.inStock)
        assertEquals(AddEditProductBundle.ProductAction.ADD, addProductBundle?.actionType)
    }

    @Test
    fun testGetAddEditProductBundle_ValidBundle_ReturnBundle() {
        val addEditProduct = AddEditProductBundle(
            productId = 1,
            name = "Product Bundle",
            inStock = true,
            actionType = AddEditProductBundle.ProductAction.ADD
        )
        val productBundle = Bundle()
        productBundle.putParcelable("addEditProduct", addEditProduct)
        val bundledProduct = bundler.getAddEditProductBundle(productBundle)
        assertEquals(addEditProduct, bundledProduct)
    }

    @Test
    fun testGetAddEditProductBundle_InvalidBundle_ReturnDefaultProductBundle() {
        val addEditProduct = AddEditProductBundle()
        val bundledProduct = bundler.getAddEditProductBundle(Bundle())
        assertEquals(addEditProduct, bundledProduct)
    }

    @Test
    fun testMakeEditLocationBundle_LocationIdProvided_EditObjectBundled() {
        val locationId = 1
        val bundle = bundler.makeEditLocationBundle(locationId)

        val editLocationBundle =
            getParcelableBundle(bundle, "addEditLocation", AddEditLocationBundle::class.java)

        assertNotNull(editLocationBundle)
        assertEquals(1, editLocationBundle?.locationId)
        assertEquals(AddEditLocationBundle.LocationAction.EDIT, editLocationBundle?.actionType)
    }

    @Test
    fun testMakeAddLocationBundle_NoAttributesProvided_AddObjectBundled() {
        val bundle = bundler.makeAddLocationBundle()

        val addLocationBundle =
            getParcelableBundle(bundle, "addEditLocation", AddEditLocationBundle::class.java)

        assertNotNull(addLocationBundle)
        assertNull(addLocationBundle?.name)
        assertEquals(AddEditLocationBundle.LocationAction.ADD, addLocationBundle?.actionType)
    }

    @Test
    fun testMakeAddLocationBundle_NameProvided_AddObjectBundleHasName() {
        val locationName = "Location Name"
        val bundle = bundler.makeAddLocationBundle(locationName)

        val addLocationBundle =
            getParcelableBundle(bundle, "addEditLocation", AddEditLocationBundle::class.java)

        assertEquals(locationName, addLocationBundle?.name)
        assertEquals(AddEditLocationBundle.LocationAction.ADD, addLocationBundle?.actionType)
    }

    @Test
    fun testGetAddEditLocationBundle_ValidBundle_ReturnBundle() {
        val addEditLocation = AddEditLocationBundle(
            locationId = 1,
            name = "Location Bundle",
            locationType = LocationType.SHOP,
            actionType = AddEditLocationBundle.LocationAction.ADD
        )
        val locationBundle = Bundle()
        locationBundle.putParcelable("addEditLocation", addEditLocation)
        val bundledLocation = bundler.getAddEditLocationBundle(locationBundle)
        assertEquals(addEditLocation, bundledLocation)
        assertEquals(addEditLocation.locationType, bundledLocation.locationType)
    }

    @Test
    fun testGetAddEditLocationBundle_InvalidBundle_ReturnDefaultLocationBundle() {
        val addEditLocation = AddEditLocationBundle()
        val bundledLocation = bundler.getAddEditLocationBundle(Bundle())
        assertEquals(addEditLocation, bundledLocation)
    }

    @Test
    fun testGetAddEditLocationBundle_NullBundle_ReturnDefaultLocationBundle() {
        val addEditLocation = AddEditLocationBundle()
        val bundledLocation = bundler.getAddEditLocationBundle(null)
        assertEquals(addEditLocation, bundledLocation)
    }

    @Test
    fun testMakeShoppingListBundle_ParametersProvided_ShoppingListBundleReturned() {
        val locationId = 123
        val filterType = FilterType.NEEDED
        val bundle = bundler.makeShoppingListBundle(locationId, filterType)

        val shoppingListBundle =
            getParcelableBundle(bundle, "shoppingList", ShoppingListBundle::class.java)

        assertEquals(locationId, shoppingListBundle?.locationId)
        assertEquals(filterType, shoppingListBundle?.filterType)
    }


    @Test
    fun testGetShoppingListBundle_ValidBundle_ReturnBundle() {
        val shoppingListBundle = ShoppingListBundle(
            locationId = 123,
            filterType = FilterType.NEEDED
        )
        val bundle = Bundle()
        bundle.putParcelable("shoppingList", shoppingListBundle)
        val bundledShoppingList = bundler.getShoppingListBundle(bundle)
        assertEquals(shoppingListBundle, bundledShoppingList)
    }

    @Test
    fun testGetShoppingListBundle_InvalidBundle_ReturnDefaultShoppingListBundle() {
        val shoppingListBundle = ShoppingListBundle(null, null)
        val bundledShoppingList = bundler.getShoppingListBundle(Bundle())
        assertEquals(shoppingListBundle, bundledShoppingList)
    }

    @Test
    fun testGetShoppingListBundle_NullBundle_ReturnDefaultShoppingListBundle() {
        val shoppingListBundle = ShoppingListBundle(null, null)
        val bundledShoppingList = bundler.getShoppingListBundle(null)
        assertEquals(shoppingListBundle, bundledShoppingList)
    }

    @Test
    fun testGetShoppingListBundle_BundledAttributes_ReturnBundle() {
        val shoppingListBundle = ShoppingListBundle(
            locationId = 123,
            filterType = FilterType.IN_STOCK
        )
        val bundle = Bundle()
        bundle.putInt("locationId", shoppingListBundle.locationId)
        bundle.putSerializable("filterType", shoppingListBundle.filterType)
        val bundledShoppingList = bundler.getShoppingListBundle(bundle)
        assertEquals(shoppingListBundle, bundledShoppingList)
    }

    @Test
    fun makeAddProductBundle_AisleIdProvided_BundleHasAisleId() {
        val aisleId = 12
        val bundle = bundler.makeAddProductBundle(aisleId = aisleId)

        val addProductBundle =
            getParcelableBundle(bundle, "addEditProduct", AddEditProductBundle::class.java)

        assertEquals(aisleId, addProductBundle!!.aisleId)
    }
}