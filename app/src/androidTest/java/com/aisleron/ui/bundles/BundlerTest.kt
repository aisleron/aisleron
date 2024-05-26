package com.aisleron.ui.bundles

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
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

        val editProductBundle = bundler.getAddEditProductBundle(bundle)

        assertNotNull(editProductBundle)
        assertEquals(1, editProductBundle.productId)
        assertEquals(AddEditProductBundle.ProductAction.EDIT, editProductBundle.actionType)
    }

    @Test
    fun makeAddProductBundle() {
    }

    @Test
    fun getAddEditProductBundle() {
    }

    @Test
    fun makeEditLocationBundle() {
    }

    @Test
    fun makeAddLocationBundle() {
    }

    @Test
    fun getAddEditLocationBundle() {
    }

    @Test
    fun makeEditProductBundle() {
    }
}