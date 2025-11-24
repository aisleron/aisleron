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

package com.aisleron.ui.resourceprovider

import android.content.Context
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes

class ResourceProviderTestImpl : ResourceProvider {
    // Key: resId (Int), Value: Any (Int, String, Boolean, etc.)
    private val overrides = mutableMapOf<Int, Any>()

    fun setOverride(resId: Int, value: Any) {
        overrides[resId] = value
    }

    fun clearOverrides() {
        overrides.clear()
    }

    override fun getInteger(context: Context, @IntegerRes resId: Int): Int {
        return overrides[resId] as? Int ?: context.resources.getInteger(resId)
    }

    override fun getString(context: Context, @StringRes resId: Int): String {
        return overrides[resId] as? String ?: context.resources.getString(resId)
    }

    override fun getBoolean(context: Context, @BoolRes resId: Int): Boolean {
        return overrides[resId] as? Boolean ?: context.resources.getBoolean(resId)
    }

    override fun getColor(context: Context, @ColorRes resId: Int): Int {
        return overrides[resId] as? Int ?: context.getColor(resId)
    }
}