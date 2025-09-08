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

package com.aisleron

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation

class SharedPreferencesInitializer {

    enum class ApplicationTheme(val key: String) {
        SYSTEM_THEME("system_theme"),
        LIGHT_THEME("light_theme"),
        DARK_THEME("dark_theme"),
    }

    enum class TrackingMode(val value: String) {
        CHECKBOX("checkbox"),
        QUANTITY("quantity"),
        CHECKBOX_QUANTITY("checkbox_quantity"),
        NONE("none")
    }

    private fun getPreferencesEditor(): SharedPreferences.Editor {
        val targetContext = getInstrumentation().targetContext
        return PreferenceManager.getDefaultSharedPreferences(targetContext).edit()
    }

    fun setIsInitialized(isInitialized: Boolean) {
        val preferencesEditor = getPreferencesEditor()

        // we can clear(), putString(key, value: String)
        // putInt, putLong, putBoolean, ...
        // after function, need to commit() the changes.
        preferencesEditor.clear()
        preferencesEditor.putBoolean(IS_INITIALIZED, isInitialized)
        preferencesEditor.commit()

    }

    fun setApplicationTheme(applicationTheme: ApplicationTheme) {
        val preferencesEditor = getPreferencesEditor()

        preferencesEditor.clear()
        preferencesEditor.putString(APPLICATION_THEME, applicationTheme.key)
        preferencesEditor.commit()
    }

    fun setHideStatusChangeSnackBar(hideStatusChangeSnackBar: Boolean) {
        val preferencesEditor = getPreferencesEditor()

        preferencesEditor.clear()
        preferencesEditor.putBoolean(PREF_HIDE_STATUS_CHANGE_SNACK_BAR, hideStatusChangeSnackBar)
        preferencesEditor.commit()
    }

    fun setShowEmptyAisles(showEmptyAisles: Boolean) {
        val preferencesEditor = getPreferencesEditor()

        preferencesEditor.clear()
        preferencesEditor.putBoolean(PREF_SHOW_EMPTY_AISLES, showEmptyAisles)
        preferencesEditor.commit()
    }

    fun setTrackingMode(trackingMode: TrackingMode) {
        val preferencesEditor = getPreferencesEditor()

        preferencesEditor.clear()
        preferencesEditor.putString(PREF_TRACKING_MODE, trackingMode.value)
        preferencesEditor.commit()

    }

    fun setKeepScreenOn(keepScreenOn: Boolean) {
        val preferencesEditor = getPreferencesEditor()

        preferencesEditor.clear()
        preferencesEditor.putBoolean(PREF_KEEP_SCREEN_ON, keepScreenOn)
        preferencesEditor.commit()

    }

    companion object {
        private const val IS_INITIALIZED = "is_initialised"
        private const val APPLICATION_THEME = "application_theme"
        private const val PREF_HIDE_STATUS_CHANGE_SNACK_BAR = "hide_status_change_snack_bar"
        private const val PREF_SHOW_EMPTY_AISLES = "show_empty_aisles"
        private const val PREF_TRACKING_MODE = "tracking_mode"
        private const val PREF_KEEP_SCREEN_ON = "keep_screen_on"
    }
}