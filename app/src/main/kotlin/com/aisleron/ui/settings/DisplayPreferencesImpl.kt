package com.aisleron.ui.settings

import android.content.SharedPreferences

class DisplayPreferencesImpl(private val sharedPreferences: SharedPreferences) :
    DisplayPreferences {

    override val showOnLockScreen: Boolean
        get() = sharedPreferences.getBoolean(DISPLAY_LOCKSCREEN, false)

    override val applicationTheme: DisplayPreferences.ApplicationTheme
        get() {
            val appTheme = sharedPreferences.getString(APPLICATION_THEME, SYSTEM_THEME)

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