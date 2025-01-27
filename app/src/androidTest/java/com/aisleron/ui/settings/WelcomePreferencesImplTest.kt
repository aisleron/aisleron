package com.aisleron.ui.settings

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.SharedPreferencesInitializer
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WelcomePreferencesImplTest {

    @Test
    fun getInitializedStatus_isInitialized_ReturnTrue() {
        SharedPreferencesInitializer().setIsInitialized(true)
        val isInitialized =
            WelcomePreferencesImpl().isInitialized(getInstrumentation().targetContext)

        assertTrue(isInitialized)
    }

    @Test
    fun getInitializedStatus_isNotInitialized_ReturnFalse() {
        SharedPreferencesInitializer().setIsInitialized(false)
        val isInitialized =
            WelcomePreferencesImpl().isInitialized(getInstrumentation().targetContext)

        assertFalse(isInitialized)
    }

    @Test
    fun setInitialised_MethodCalled_InitializedIsTrue() {
        SharedPreferencesInitializer().setIsInitialized(false)

        WelcomePreferencesImpl().setInitialised(getInstrumentation().targetContext)
        val isInitialized =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)
                .getBoolean("is_initialised", false)

        assertTrue(isInitialized)
    }
}