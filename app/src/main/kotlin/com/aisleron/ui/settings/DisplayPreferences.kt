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

package com.aisleron.ui.settings

import android.content.Context
import com.aisleron.ui.bundles.ShoppingListBundle

interface DisplayPreferences {

    enum class ApplicationTheme {
        SYSTEM_THEME,
        LIGHT_THEME,
        DARK_THEME
    }

    enum class PureBlackStyle {
        DEFAULT,
        ECONOMY,
        BUSINESS_CLASS,
        FIRST_CLASS
    }

    fun showOnLockScreen(context: Context): Boolean

    fun applicationTheme(context: Context): ApplicationTheme

    fun startingList(context: Context): ShoppingListBundle

    fun dynamicColor(context: Context): Boolean

    fun pureBlackStyle(context: Context): PureBlackStyle
}