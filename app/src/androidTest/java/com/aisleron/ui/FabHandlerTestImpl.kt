package com.aisleron.ui

import android.app.Activity
import android.view.View

class FabHandlerTestImpl : FabHandler {
    private val fabOnClick = mutableMapOf<FabHandler.FabOption, View.OnClickListener>()
    override fun getFabView(activity: Activity): View? = null

    override fun setFabOnClickListener(
        activity: Activity,
        fabOption: FabHandler.FabOption,
        onClickListener: View.OnClickListener
    ) {
        fabOnClick[fabOption] = onClickListener
    }

    override fun setFabItems(activity: Activity, vararg fabOptions: FabHandler.FabOption) {
        fabOnClick.clear()
    }

    fun clickFab(fabOption: FabHandler.FabOption, view: View) {
        fabOnClick[fabOption]?.onClick(view)
    }
}