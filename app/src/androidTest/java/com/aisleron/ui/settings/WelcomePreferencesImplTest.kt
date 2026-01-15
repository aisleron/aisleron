/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.ui.settings

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.BuildConfig
import com.aisleron.SharedPreferencesInitializer
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WelcomePreferencesImplTest {
    val welcomePreferences: WelcomePreferencesImpl
        get() = WelcomePreferencesImpl(getInstrumentation().targetContext)

    @Before
    fun setUp() {
        SharedPreferencesInitializer().clearPreferences()
    }

    @Test
    fun getInitializedStatus_isInitialized_ReturnTrue() {
        SharedPreferencesInitializer().setIsInitialized(true)
        val isInitialized =
            welcomePreferences.isInitialized()

        assertTrue(isInitialized)
    }

    @Test
    fun getInitializedStatus_isNotInitialized_ReturnFalse() {
        SharedPreferencesInitializer().setIsInitialized(false)
        val isInitialized =
            welcomePreferences.isInitialized()

        assertFalse(isInitialized)
    }

    @Test
    fun setInitialised_MethodCalled_InitializedIsTrue() {
        SharedPreferencesInitializer().setIsInitialized(false)

        welcomePreferences.setInitialised()
        val isInitialized =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)
                .getBoolean("is_initialised", false)

        assertTrue(isInitialized)
    }

    @Test
    fun getLastUpdateVersionCode_NoValueDefined_ReturnDefaultValue() {
        val defaultValue = 11

        val lastUpdateVersionCode =
            welcomePreferences.getLastUpdateVersionCode()

        assertEquals(defaultValue, lastUpdateVersionCode)
    }

    @Test
    fun getLastUpdateVersionCode_ValueDefined_ReturnValue() {
        val versionCode = 15
        SharedPreferencesInitializer().setLastUpdateCode(versionCode)

        val lastUpdateVersionCode =
            welcomePreferences.getLastUpdateVersionCode()

        assertEquals(versionCode, lastUpdateVersionCode)
    }

    @Test
    fun getLastUpdateVersionName_NoValueDefined_ReturnDefaultValue() {
        val defaultValue = "2025.8.0"

        val lastUpdateVersionCode =
            welcomePreferences.getLastUpdateVersionName()

        assertEquals(defaultValue, lastUpdateVersionCode)
    }

    @Test
    fun getLastUpdateVersionName_ValueDefined_ReturnValue() {
        val versionName = "2025.100.0"
        SharedPreferencesInitializer().setLastUpdateName(versionName)

        val lastUpdateVersionName =
            welcomePreferences.getLastUpdateVersionName()

        assertEquals(versionName, lastUpdateVersionName)
    }

    @Test
    fun setLastUpdateValues_NoValuesProvided_SetFromBuildConfig() {
        val versionCode = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME

        welcomePreferences.setLastUpdateValues()

        val prefs =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)

        val updatedVersionCode = prefs.getInt("last_update_code", 0)
        assertEquals(versionCode, updatedVersionCode)

        val updatedVersionName = prefs.getString("last_update_name", "")
        assertEquals(versionName, updatedVersionName)
    }

    @Test
    fun setLastUpdateValues_ValuesProvided_SetToProvidedValues() {
        val versionCode = -3
        val versionName = "2025.100.0"

        welcomePreferences.setLastUpdateValues(versionCode, versionName)

        val prefs =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)

        val updatedVersionCode = prefs.getInt("last_update_code", 0)
        assertEquals(versionCode, updatedVersionCode)

        val updatedVersionName = prefs.getString("last_update_name", "")
        assertEquals(versionName, updatedVersionName)
    }
}