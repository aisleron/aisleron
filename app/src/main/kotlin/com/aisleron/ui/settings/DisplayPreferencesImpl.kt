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