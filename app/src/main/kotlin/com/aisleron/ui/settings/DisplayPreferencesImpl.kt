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
import com.aisleron.domain.preferences.ApplicationTheme
import com.aisleron.domain.preferences.PureBlackStyle
import com.aisleron.ui.bundles.ShoppingListBundle

class DisplayPreferencesImpl(context: Context) : DisplayPreferences {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    override fun showOnLockScreen(): Boolean =
        prefs.getBoolean(DISPLAY_LOCKSCREEN, false)

    override fun applicationTheme(): ApplicationTheme {
        val appTheme = prefs.getString(
            APPLICATION_THEME, ApplicationTheme.SYSTEM_THEME.value
        )

        return ApplicationTheme.fromValue(appTheme)
    }

    override fun startingList(): ShoppingListBundle {
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

    override fun dynamicColor(): Boolean =
        prefs.getBoolean(DYNAMIC_COLOR, false)

    override fun pureBlackStyle(): PureBlackStyle {
        val pureBlackStyle = prefs
            .getString(PURE_BLACK_STYLE, PureBlackStyle.DEFAULT.value)

        return PureBlackStyle.fromValue(pureBlackStyle)
    }

    companion object {
        private const val DISPLAY_LOCKSCREEN = "display_lockscreen"
        private const val APPLICATION_THEME = "application_theme"
        private const val DYNAMIC_COLOR = "dynamic_color"
        private const val PURE_BLACK_STYLE = "pure_black_style"
        private const val STARTING_LIST = "starting_list"
    }
}