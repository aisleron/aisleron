/*
 * Copyright (C) 2025 aisleron.com
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
    private val fabOnClick = mutableMapOf<FabHandler.FabOption, View.OnClickListener>()
    override fun getFabView(activity: Activity): View? = null

    private var _fabClickedCallBack: FabClickedCallBack? = null
    override fun setFabOnClickedListener(fabClickedCallBack: FabClickedCallBack) {
        _fabClickedCallBack = fabClickedCallBack
    }

    override fun setFabOnClickListener(
        activity: Activity,
        fabOption: FabHandler.FabOption,
        onClickListener: View.OnClickListener
    ) {
        fabOnClick[fabOption] = View.OnClickListener {
            onClickListener.onClick(it)
            _fabClickedCallBack?.fabClicked(fabOption)
        }
    }

    override fun setFabItems(activity: Activity, vararg fabOptions: FabHandler.FabOption) {
        fabOnClick.clear()
    }

    fun clickFab(fabOption: FabHandler.FabOption, view: View) {
        fabOnClick[fabOption]?.onClick(view)
    }
}