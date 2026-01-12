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
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductPreferencesImplTest {
    val productPreferences: ProductPreferencesImpl
        get() = ProductPreferencesImpl(getInstrumentation().targetContext)

    @Before
    fun setUp() {
        SharedPreferencesInitializer().clearPreferences()
    }

    @Test
    fun showExtraOptions_isExtraOptionsActive_ReturnTrue() {
        SharedPreferencesInitializer().setShowProductExtraOptions(true)
        val showExtraOptions =
            productPreferences.showExtraOptions()

        assertTrue(showExtraOptions)
    }

    @Test
    fun showExtraOptions_isExtraOptionsInactive_ReturnFalse() {
        SharedPreferencesInitializer().setShowProductExtraOptions(false)
        val showExtraOptions =
            productPreferences.showExtraOptions()

        assertFalse(showExtraOptions)
    }

    @Test
    fun setShowExtraOptions_SetToTrue_ShowExtraOptionsIsTrue() {
        SharedPreferencesInitializer().setShowProductExtraOptions(false)

        productPreferences.setShowExtraOptions(true)
        val showExtraOptions =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)
                .getBoolean("show_product_extra_options", false)

        assertTrue(showExtraOptions)
    }

    @Test
    fun setShowExtraOptions_SetToFalse_ShowExtraOptionsIsFalse() {
        SharedPreferencesInitializer().setShowProductExtraOptions(true)

        productPreferences.setShowExtraOptions(false)
        val showExtraOptions =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)
                .getBoolean("show_product_extra_options", true)

        assertFalse(showExtraOptions)
    }

    @Test
    fun getLastSelectedTab_PositionSet_PositionReturned() {
        SharedPreferencesInitializer().setProductLastSelectedTab(1)

        val position = productPreferences.getLastSelectedTab()

        assertEquals(1, position)
    }

    @Test
    fun setLastSelectedTab_SetToValue_ValueSavedAsPosition() {
        SharedPreferencesInitializer().setProductLastSelectedTab(0)

        productPreferences.setLastSelectedTab(2)
        val lastSelectedTabPosition =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)
                .getInt("product_last_selected_tab", 0)

        assertEquals(2, lastSelectedTabPosition)
    }

}