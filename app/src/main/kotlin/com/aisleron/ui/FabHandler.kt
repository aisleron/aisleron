package com.aisleron.ui

import android.view.View

interface FabHandler {
    fun setFabOnClickListener(fabOption: FabOption, onClickListener: View.OnClickListener)
    fun setFabItems(vararg fabOptions: FabOption)

    enum class FabOption {
        ADD_PRODUCT, ADD_AISLE, ADD_SHOP
    }
}