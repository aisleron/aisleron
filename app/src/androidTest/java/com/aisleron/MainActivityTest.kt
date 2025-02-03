package com.aisleron

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.di.TestAppModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import kotlin.test.assertEquals

class MainActivityTest {
    private lateinit var testData: TestDataManager

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> {
        testData = TestDataManager(false)
        return TestAppModules().getTestAppModules(testData)
    }

    @Before
    fun setUp() {
    }

    @Test
    fun appStart_LightThemeSet_UseLightTheme() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.LIGHT_THEME)

        ActivityScenario.launch(MainActivity::class.java)
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun appStart_DarkThemeSet_UseDarkTheme() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.DARK_THEME)
        ActivityScenario.launch(MainActivity::class.java)
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun appStart_SystemThemeSet_UseSystemTheme() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.SYSTEM_THEME)
        ActivityScenario.launch(MainActivity::class.java)
        assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.getDefaultNightMode()
        )
    }

}