/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.data.preferences

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.BuildConfig
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.domain.preferences.SyncPreferences
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SyncPreferencesImplTest {
    private lateinit var syncPreferences: SyncPreferences
    private lateinit var sharedPreferencesInitializer: SharedPreferencesInitializer

    @Before
    fun setUp() {
        sharedPreferencesInitializer = SharedPreferencesInitializer()
        syncPreferences = SyncPreferencesImpl(getInstrumentation().targetContext)
        sharedPreferencesInitializer.clearPreferences()
    }

    @Test
    fun useDefaultBackend_returnsCorrectBoolean_forGivenSetting() {
        listOf(true, false).forEach { useDefaultBackend ->
            sharedPreferencesInitializer.setUseDefaultBackend(useDefaultBackend)
            val actual = syncPreferences.useDefaultBackEnd()
            assertEquals(
                useDefaultBackend, actual, "Failed for useDefaultBackend: $useDefaultBackend"
            )
        }
    }

    @Test
    fun getBackendUrl_UseDefaultBackEnd_ReturnsDefaultBackendUrl() {
        sharedPreferencesInitializer.setUseDefaultBackend(true)
        sharedPreferencesInitializer.setCustomBackendUrl("https://a.custom.url")

        val backendUrl = syncPreferences.getBackendUrl()

        assertEquals(BuildConfig.SUPABASE_URL, backendUrl)
    }

    @Test
    fun getBackendUrl_UseCustomBackEnd_ReturnsCustomBackendUrl() {
        val customBackendUrl = "https://a.custom.url"
        sharedPreferencesInitializer.setUseDefaultBackend(false)
        sharedPreferencesInitializer.setCustomBackendUrl(customBackendUrl)

        val backendUrl = syncPreferences.getBackendUrl()

        assertEquals(customBackendUrl, backendUrl)
    }

    @Test
    fun getBackendKey_UseDefaultBackEnd_ReturnsDefaultBackendKey() {
        sharedPreferencesInitializer.setUseDefaultBackend(true)
        sharedPreferencesInitializer.setCustomBackendKey("123abc")

        val backendKey = syncPreferences.getBackendKey()

        assertEquals(BuildConfig.SUPABASE_ANON_KEY, backendKey)
    }

    @Test
    fun getBackendKey_UseCustomBackEnd_ReturnsCustomBackendKey() {
        val customBackendKey = "123abc"
        sharedPreferencesInitializer.setUseDefaultBackend(false)
        sharedPreferencesInitializer.setCustomBackendKey(customBackendKey)

        val backendKey = syncPreferences.getBackendKey()

        assertEquals(customBackendKey, backendKey)
    }

    /**
     * Test getBackendUrl for default Backend
     * Test getBackendUrl for custom Backend
     * Test getBackendKey for default Backend
     * Test getBackendKey for custom Backend
     */

}