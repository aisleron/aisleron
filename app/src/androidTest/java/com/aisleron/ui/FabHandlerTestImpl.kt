/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.ui

import android.app.Activity
import android.view.View
import com.aisleron.ui.FabHandler.FabClickedCallBack

class FabHandlerTestImpl : FabHandler {
    private var fabItems = mutableListOf<FabHandler.FabOption>()
    private val fabOnClick = mutableMapOf<FabHandler.FabOption, View.OnClickListener>()
    override fun getFabView(activity: Activity): View? = null

    private var _fabClickedCallBack: FabClickedCallBack? = null
    override fun setFabOnClickedListener(fabClickedCallBack: FabClickedCallBack) {
        _fabClickedCallBack = fabClickedCallBack
    }

    override fun setFabItems(activity: Activity, vararg fabOptions: FabHandler.FabOption) {
        fabItems.clear()
        fabItems.addAll(fabOptions)
    }

    override fun reset() {
        fabOnClick.clear()
    }

    fun clickFab(fabOption: FabHandler.FabOption) {
        _fabClickedCallBack?.fabClicked(fabOption)
    }

    fun getFabItems(): List<FabHandler.FabOption> = fabItems
}