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

    companion object {
        private const val IS_INITIALIZED = "is_initialised"
        private const val APPLICATION_THEME = "application_theme"
        private const val PREF_HIDE_STATUS_CHANGE_SNACK_BAR = "hide_status_change_snack_bar"
    }
}