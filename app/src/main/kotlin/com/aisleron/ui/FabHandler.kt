package com.aisleron.ui

import android.view.View

interface FabHandler {
    var allFabAreHidden: Boolean
    fun hideAllFab()
    fun showAllFab()
    fun initializeFab()
    fun setFabOnClickListener(
        fabOption: FabOption,
        onClickListener: View.OnClickListener
    )

    fun setModeShowAllFab()

    enum class FabOption {
        ADD_PRODUCT, ADD_AISLE, ADD_SHOP
    }
}