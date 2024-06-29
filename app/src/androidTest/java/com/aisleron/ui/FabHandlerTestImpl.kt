package com.aisleron.ui

import android.view.View

class FabHandlerTestImpl : FabHandler {
    override var allFabAreHidden: Boolean = true
    private val fabOnClick = mutableMapOf<FabHandler.FabOption, View.OnClickListener>()

    override fun setFabOnClickListener(
        fabOption: FabHandler.FabOption,
        onClickListener: View.OnClickListener
    ) {
        fabOnClick[fabOption] = onClickListener
    }

    override fun setFabItems(vararg fabOptions: FabHandler.FabOption) {
        fabOnClick.clear()
    }

    fun clickFab(fabOption: FabHandler.FabOption, view: View) {
        fabOnClick[fabOption]?.onClick(view)
    }
}