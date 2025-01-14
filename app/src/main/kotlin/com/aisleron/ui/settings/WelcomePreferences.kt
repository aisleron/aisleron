package com.aisleron.ui.settings

import android.content.Context

interface WelcomePreferences {

    fun isInitialized(context: Context): Boolean

    fun setInitialised(context: Context)
}