package com.aisleron.ui

import android.view.View

class FabHandlerTestImpl : FabHandler {
    override var allFabAreHidden: Boolean = true
    private val fabOnClick = mutableMapOf<FabHandler.FabOption, View.OnClickListener>()

    override fun hideAllFab() {}

    override fun showAllFab() {}

    override fun initializeFab() {
        fabOnClick.clear()
    }

    override fun setFabOnClickListener(
        fabOption: FabHandler.FabOption,
        onClickListener: View.OnClickListener
    ) {
        fabOnClick[fabOption] = onClickListener
    }

    override fun setModeShowAllFab() {}
    override fun setModeShowAddShopFabOnly() {}
    override fun setModeShowNoFab() {}

    fun clickFab(fabOption: FabHandler.FabOption, view: View) {
        fabOnClick[fabOption]?.onClick(view)
    }
}