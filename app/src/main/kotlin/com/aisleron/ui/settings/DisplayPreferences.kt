package com.aisleron.ui.settings

import android.content.Context

interface DisplayPreferences {

    enum class ApplicationTheme {
        SYSTEM_THEME,
        LIGHT_THEME,
        DARK_THEME
    }

    fun showOnLockScreen(context: Context): Boolean

    fun applicationTheme(context: Context): ApplicationTheme
}