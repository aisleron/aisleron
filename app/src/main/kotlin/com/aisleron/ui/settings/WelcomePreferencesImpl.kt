package com.aisleron.ui.settings

import android.content.SharedPreferences

class WelcomePreferencesImpl(private val sharedPreferences: SharedPreferences) :
    WelcomePreferences {

    override val isInitialized: Boolean
        get() {
            return sharedPreferences.getBoolean(IS_INITIALIZED, false)
        }

    override fun setInitialised() {
        sharedPreferences.edit()
            ?.putBoolean(IS_INITIALIZED, true)
            ?.apply()
    }

    companion object {
        private const val IS_INITIALIZED = "is_initialised"
    }
}