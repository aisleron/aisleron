package com.aisleron.ui.settings

import android.content.Context
import androidx.preference.PreferenceManager

class WelcomePreferencesImpl() :
    WelcomePreferences {

    override fun isInitialized(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(IS_INITIALIZED, false)

    override fun setInitialised(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            ?.putBoolean(IS_INITIALIZED, true)
            ?.apply()
    }

    companion object {
        private const val IS_INITIALIZED = "is_initialised"
    }
}