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
import com.aisleron.domain.FilterType

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

    private fun <T> setPreferenceValue(name: String, value: T) {
        val preferencesEditor = getPreferencesEditor()
        when (value) {
            is Boolean -> preferencesEditor.putBoolean(name, value)
            is String -> preferencesEditor.putString(name, value)
            is Int -> preferencesEditor.putInt(name, value)
        }
        preferencesEditor.commit()
    }

    fun setIsInitialized(isInitialized: Boolean) {
        setPreferenceValue(IS_INITIALIZED, isInitialized)
    }

    fun setApplicationTheme(applicationTheme: ApplicationTheme) {
        setPreferenceValue(APPLICATION_THEME, applicationTheme.key)
    }

    fun setHideStatusChangeSnackBar(hideStatusChangeSnackBar: Boolean) {
        setPreferenceValue(PREF_HIDE_STATUS_CHANGE_SNACK_BAR, hideStatusChangeSnackBar)
    }

    fun setShowOnLockScreen(showOnLockscreen: Boolean) {
        setPreferenceValue(PREF_DISPLAY_LOCKSCREEN, showOnLockscreen)
    }

    fun setShowEmptyAisles(showEmptyAisles: Boolean) {
        setPreferenceValue(PREF_SHOW_EMPTY_AISLES, showEmptyAisles)
    }

    fun setTrackingMode(trackingMode: TrackingMode) {
        setPreferenceValue(PREF_TRACKING_MODE, trackingMode.value)
    }

    fun setKeepScreenOn(keepScreenOn: Boolean) {
        setPreferenceValue(PREF_KEEP_SCREEN_ON, keepScreenOn)
    }

    fun setStartingList(locationId: Int, filterType: FilterType) {
        setStartingList("${locationId}|${filterType.name}")
    }

    fun setStartingList(value: String) {
        setPreferenceValue(PREF_STARTING_LIST, value)
    }

    fun clearPreferences() {
        val preferencesEditor = getPreferencesEditor()
        preferencesEditor.clear()
        preferencesEditor.commit()
    }

    fun setShowProductExtraOptions(value: Boolean) {
        setPreferenceValue(PREF_SHOW_PRODUCT_EXTRA_OPTIONS, value)
    }

    fun setShowShopExtraOptions(value: Boolean) {
        setPreferenceValue(PREF_SHOW_SHOP_EXTRA_OPTIONS, value)
    }

    fun setLastUpdateCode(value: Int) {
        setPreferenceValue(PREF_LAST_UPDATE_CODE, value)
    }

    fun setLastUpdateName(value: String) {
        setPreferenceValue(PREF_LAST_UPDATE_NAME, value)
    }

    companion object {
        private const val IS_INITIALIZED = "is_initialised"
        private const val APPLICATION_THEME = "application_theme"
        private const val PREF_HIDE_STATUS_CHANGE_SNACK_BAR = "hide_status_change_snack_bar"
        private const val PREF_SHOW_EMPTY_AISLES = "show_empty_aisles"
        private const val PREF_TRACKING_MODE = "tracking_mode"
        private const val PREF_KEEP_SCREEN_ON = "keep_screen_on"
        private const val PREF_STARTING_LIST = "starting_list"
        private const val PREF_DISPLAY_LOCKSCREEN = "display_lockscreen"
        private const val PREF_SHOW_PRODUCT_EXTRA_OPTIONS = "show_product_extra_options"
        private const val PREF_SHOW_SHOP_EXTRA_OPTIONS = "show_shop_extra_options"
        private const val PREF_LAST_UPDATE_CODE = "last_update_code"
        private const val PREF_LAST_UPDATE_NAME = "last_update_name"
    }
}