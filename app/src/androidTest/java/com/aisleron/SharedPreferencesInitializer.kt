package com.aisleron

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation

class SharedPreferencesInitializer {

    enum class ApplicationTheme(val key: String) {
        SYSTEM_THEME("system_theme"),
        LIGHT_THEME("light_theme"),
        DARK_THEME("dark_theme"),
    }

    fun setIsInitialized(isInitialized: Boolean) {
        val targetContext = getInstrumentation().targetContext
        val preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()

        // we can clear(), putString(key, value: String)
        // putInt, putLong, putBoolean, ...
        // after function, need to commit() the changes.
        preferencesEditor.clear()
        preferencesEditor.putBoolean(IS_INITIALIZED, isInitialized)
        preferencesEditor.commit()

    }

    fun setApplicationTheme(applicationTheme: ApplicationTheme) {
        val targetContext = getInstrumentation().targetContext
        val preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()

        preferencesEditor.clear()
        preferencesEditor.putString(APPLICATION_THEME, applicationTheme.key)
        preferencesEditor.commit()
    }

    companion object {
        private const val IS_INITIALIZED = "is_initialised"
        private const val APPLICATION_THEME = "application_theme"
    }
}