package com.aisleron.ui

import android.app.Activity
import android.view.View

interface FabHandler {
    fun getFabView(activity: Activity): View?

    fun setFabOnClickListener(
        activity: Activity,
        fabOption: FabOption,
        onClickListener: View.OnClickListener
    )

    fun setFabItems(activity: Activity, vararg fabOptions: FabOption)

    enum class FabOption {
        ADD_PRODUCT, ADD_AISLE, ADD_SHOP
    }
}