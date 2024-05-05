package com.aisleron.ui.bundles

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

class Bundler {

    private fun <T> getParcelableBundle(bundle: Bundle?, key: String, clazz: Class<T>): T? {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelable(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            bundle?.getParcelable(key)
        }
        return result
    }

    private fun makeParcelableBundle(key: String, value: Parcelable): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(key, value)
        return bundle
    }

    fun makeEditProductBundle(productId: Int): Bundle {
        val editProductBundle = AddEditProductBundle(
            productId = productId,
            actionType = AddEditProductBundle.ProductAction.EDIT
        )
        return makeParcelableBundle(ADD_EDIT_PRODUCT, editProductBundle)
    }

    fun makeAddProductBundle(name: String?, inStock: Boolean?): Bundle {
        val addProductBundle = AddEditProductBundle(
            name = name,
            inStock = inStock ?: false,
            actionType = AddEditProductBundle.ProductAction.ADD
        )
        return makeParcelableBundle(ADD_EDIT_PRODUCT, addProductBundle)
    }

    fun getAddEditProductBundle(bundle: Bundle?): AddEditProductBundle {
        val result = getParcelableBundle(bundle, ADD_EDIT_PRODUCT, AddEditProductBundle::class.java)
        return result ?: AddEditProductBundle()
    }

    private companion object BundleType {
        const val ADD_EDIT_PRODUCT = "addEditProduct"
    }
}