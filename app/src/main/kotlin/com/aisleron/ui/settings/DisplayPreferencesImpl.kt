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
import androidx.preference.PreferenceManager
import com.aisleron.domain.FilterType
import com.aisleron.ui.bundles.ShoppingListBundle

class DisplayPreferencesImpl : DisplayPreferences {

    override fun showOnLockScreen(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DISPLAY_LOCKSCREEN, false)

    override fun applicationTheme(context: Context): DisplayPreferences.ApplicationTheme {
        val appTheme = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(APPLICATION_THEME, SYSTEM_THEME)

        return when (appTheme) {
            LIGHT_THEME -> DisplayPreferences.ApplicationTheme.LIGHT_THEME
            DARK_THEME -> DisplayPreferences.ApplicationTheme.DARK_THEME
            else -> DisplayPreferences.ApplicationTheme.SYSTEM_THEME
        }
    }

    override fun startingList(context: Context): ShoppingListBundle {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val startList = prefs.getString(STARTING_LIST, null) ?: "1|IN_STOCK"
        val (locationIdStr, filterTypeStr) = startList.split("|")
        val locationId = locationIdStr.toIntOrNull() ?: 1
        val filterType = try {
            FilterType.valueOf(filterTypeStr)
        } catch (_: IllegalArgumentException) {
            FilterType.IN_STOCK
        }

        return ShoppingListBundle(locationId, filterType)
    }

    override fun dynamicColor(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DYNAMIC_COLOR, false)

    override fun pureBlackStyle(context: Context): DisplayPreferences.PureBlackStyle {
        val pureBlackStyle = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PURE_BLACK_STYLE, PURE_BLACK_DEFAULT)

        return when (pureBlackStyle) {
            PURE_BLACK_ECONOMY -> DisplayPreferences.PureBlackStyle.ECONOMY
            PURE_BLACK_BUSINESS_CLASS -> DisplayPreferences.PureBlackStyle.BUSINESS_CLASS
            PURE_BLACK_FIRST_CLASS -> DisplayPreferences.PureBlackStyle.FIRST_CLASS
            else -> DisplayPreferences.PureBlackStyle.DEFAULT
        }
    }

    companion object {
        private const val SYSTEM_THEME = "system_theme"
        private const val LIGHT_THEME = "light_theme"
        private const val DARK_THEME = "dark_theme"

        private const val DISPLAY_LOCKSCREEN = "display_lockscreen"
        private const val APPLICATION_THEME = "application_theme"
        private const val DYNAMIC_COLOR = "dynamic_color"
        private const val PURE_BLACK_STYLE = "pure_black_style"

        private const val STARTING_LIST = "starting_list"

        private const val PURE_BLACK_DEFAULT = "pure_black_default"
        private const val PURE_BLACK_ECONOMY = "pure_black_economy"
        private const val PURE_BLACK_BUSINESS_CLASS = "pure_black_business_class"
        private const val PURE_BLACK_FIRST_CLASS = "pure_black_first_class"
    }
}