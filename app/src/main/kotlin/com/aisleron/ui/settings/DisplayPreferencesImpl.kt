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

    companion object {
        private const val SYSTEM_THEME = "system_theme"
        private const val LIGHT_THEME = "light_theme"
        private const val DARK_THEME = "dark_theme"

        private const val DISPLAY_LOCKSCREEN = "display_lockscreen"
        private const val APPLICATION_THEME = "application_theme"
    }
}