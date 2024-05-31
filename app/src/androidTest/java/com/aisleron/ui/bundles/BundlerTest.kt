package com.aisleron.ui.bundles

import android.os.Bundle
import com.aisleron.domain.location.LocationType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test


class BundlerTest {
    private lateinit var bundler: Bundler

    @Before
    fun setUp() {
        bundler = Bundler()
    }

    @Test
    fun testMakeEditProductBundle_ProductIdProvided_EditObjectBundled() {
        val productId = 1
        val bundle = bundler.makeEditProductBundle(productId)

        val editProductBundle =
            bundle.getParcelable("addEditProduct", AddEditProductBundle::class.java)

        assertNotNull(editProductBundle)
        assertEquals(1, editProductBundle?.productId)
        assertEquals(AddEditProductBundle.ProductAction.EDIT, editProductBundle?.actionType)
    }

    @Test
    fun testMakeAddProductBundle_NoAttributesProvided_AddObjectBundled() {
        val bundle = bundler.makeAddProductBundle()

        val addProductBundle =
            bundle.getParcelable("addEditProduct", AddEditProductBundle::class.java)

        assertNotNull(addProductBundle)
        assertNull(addProductBundle?.name)
        assertFalse(addProductBundle?.inStock!!)
        assertEquals(AddEditProductBundle.ProductAction.ADD, addProductBundle.actionType)
    }

    @Test
    fun testMakeAddProductBundle_NameProvided_AddObjectBundleHasName() {
        val productName = "Product Name"
        val bundle = bundler.makeAddProductBundle(productName)

        val addProductBundle =
            bundle.getParcelable("addEditProduct", AddEditProductBundle::class.java)

        assertEquals(productName, addProductBundle?.name)
        assertEquals(AddEditProductBundle.ProductAction.ADD, addProductBundle?.actionType)
    }

    @Test
    fun testMakeAddProductBundle_InStockTrue_AddObjectBundleIsInStock() {
        val inStock = true
        val bundle = bundler.makeAddProductBundle(inStock = inStock)

        val addProductBundle =
            bundle.getParcelable("addEditProduct", AddEditProductBundle::class.java)

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
            bundle.getParcelable("addEditLocation", AddEditLocationBundle::class.java)

        assertNotNull(editLocationBundle)
        assertEquals(1, editLocationBundle?.locationId)
        assertEquals(AddEditLocationBundle.LocationAction.EDIT, editLocationBundle?.actionType)
    }

    @Test
    fun testMakeAddLocationBundle_NoAttributesProvided_AddObjectBundled() {
        val bundle = bundler.makeAddLocationBundle()

        val addLocationBundle =
            bundle.getParcelable("addEditLocation", AddEditLocationBundle::class.java)

        assertNotNull(addLocationBundle)
        assertNull(addLocationBundle?.name)
        assertEquals(AddEditLocationBundle.LocationAction.ADD, addLocationBundle?.actionType)
    }

    @Test
    fun testMakeAddLocationBundle_NameProvided_AddObjectBundleHasName() {
        val locationName = "Location Name"
        val bundle = bundler.makeAddLocationBundle(locationName)

        val addLocationBundle =
            bundle.getParcelable("addEditLocation", AddEditLocationBundle::class.java)

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
    }

    @Test
    fun testGetAddEditLocationBundle_InvalidBundle_ReturnDefaultLocationBundle() {
        val addEditLocation = AddEditLocationBundle()
        val bundledLocation = bundler.getAddEditLocationBundle(Bundle())
        assertEquals(addEditLocation, bundledLocation)
    }
}