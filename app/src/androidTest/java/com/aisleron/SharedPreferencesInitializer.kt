package com.aisleron

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation

class SharedPreferencesInitializer() {
    operator fun invoke(isInitialized: Boolean) {
        val targetContext = getInstrumentation().targetContext
        val preferencesEditor = PreferenceManager.getDefaultSharedPreferences(targetContext).edit()

        // we can clear(), putString(key, value: String)
        // putInt, putLong, putBoolean, ...
        // after function, need to commit() the changes.
        preferencesEditor.clear()
        preferencesEditor.putBoolean(IS_INITIALIZED, isInitialized)
        preferencesEditor.commit()

    }

    companion object {
        private const val IS_INITIALIZED = "is_initialised"
    }
}