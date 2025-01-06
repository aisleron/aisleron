package com.aisleron.ui.settings

interface DisplayPreferences {

    enum class ApplicationTheme {
        SYSTEM_THEME,
        LIGHT_THEME,
        DARK_THEME
    }

    val showOnLockScreen: Boolean

    val applicationTheme: ApplicationTheme
}