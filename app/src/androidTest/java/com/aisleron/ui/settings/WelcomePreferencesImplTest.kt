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