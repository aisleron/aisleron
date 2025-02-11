package com.aisleron.ui.settings

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.SharedPreferencesInitializer
import org.junit.Test
import kotlin.test.assertEquals

class DisplayPreferencesImplTest {

    @Test
    fun getApplicationTheme_SetToLightTheme_ReturnLightThemeEnum() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.LIGHT_THEME)
        val applicationTheme =
            DisplayPreferencesImpl().applicationTheme(getInstrumentation().targetContext)

        assertEquals(DisplayPreferences.ApplicationTheme.LIGHT_THEME, applicationTheme)
    }

    @Test
    fun getApplicationTheme_SetToDarkTheme_ReturnDarkThemeEnum() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.DARK_THEME)
        val applicationTheme =
            DisplayPreferencesImpl().applicationTheme(getInstrumentation().targetContext)

        assertEquals(DisplayPreferences.ApplicationTheme.DARK_THEME, applicationTheme)
    }

    @Test
    fun getApplicationTheme_SetToSystemTheme_ReturnSystemThemeEnum() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.SYSTEM_THEME)
        val applicationTheme =
            DisplayPreferencesImpl().applicationTheme(getInstrumentation().targetContext)

        assertEquals(DisplayPreferences.ApplicationTheme.SYSTEM_THEME, applicationTheme)
    }
}