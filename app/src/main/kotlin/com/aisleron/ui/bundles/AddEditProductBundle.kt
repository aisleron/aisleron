package com.aisleron.ui.bundles

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddEditProductBundle(
    val productId: Int = 0,
    val name: String? = null,
    val inStock: Boolean? = null,
    val actionType: ProductAction = ProductAction.ADD
) : Parcelable {
    enum class ProductAction {
        ADD, EDIT
    }
}
