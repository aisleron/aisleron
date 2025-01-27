package com.aisleron.ui.settings

import android.content.Context

class WelcomePreferencesTestImpl : WelcomePreferences {

    private var _isInitialized: Boolean = false

    override fun isInitialized(context: Context): Boolean = _isInitialized

    override fun setInitialised(context: Context) {
        _isInitialized = true
    }
}