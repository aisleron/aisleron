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

interface FabHandler {
    fun getFabView(activity: Activity): View?
    fun setFabOnClickedListener(fabClickedCallBack: FabClickedCallBack)

    fun setFabOnClickListener(
        activity: Activity,
        fabOption: FabOption,
        onClickListener: View.OnClickListener
    )

    fun setFabItems(activity: Activity, vararg fabOptions: FabOption)

    enum class FabOption {
        ADD_PRODUCT, ADD_AISLE, ADD_SHOP
    }

    interface FabClickedCallBack {
        fun fabClicked(fabOption: FabOption)
    }
}