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

interface ResourceProvider {
    fun getInteger(context: Context, @IntegerRes resId: Int): Int
    fun getString(context: Context, @StringRes resId: Int): String
    fun getBoolean(context: Context, @BoolRes resId: Int): Boolean
    fun getColor(context: Context, @ColorRes resId: Int): Int
}