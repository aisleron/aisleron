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

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.domain.FilterType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DisplayPreferencesImplTest {

    @Before
    fun setUp() {
        SharedPreferencesInitializer().clearPreferences()
    }

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

    @Test
    fun showOnLockScreen_ValueIsFalse_ReturnFalse() {
        val showOnLockScreen = false
        SharedPreferencesInitializer().setShowOnLockScreen(showOnLockScreen)
        val showOnLockScreenResult =
            DisplayPreferencesImpl().showOnLockScreen(getInstrumentation().targetContext)

        assertEquals(showOnLockScreen, showOnLockScreenResult)
    }

    @Test
    fun showOnLockScreen_ValueIsTrue_ReturnTrue() {
        val showOnLockScreen = true
        SharedPreferencesInitializer().setShowOnLockScreen(showOnLockScreen)
        val showOnLockScreenResult =
            DisplayPreferencesImpl().showOnLockScreen(getInstrumentation().targetContext)

        assertEquals(showOnLockScreen, showOnLockScreenResult)
    }

    @Test
    fun getStartingList_FilterTypeIsInStock_ReturnInStockShoppingListBundle() {
        val locationId = 1
        val filterType = FilterType.IN_STOCK
        SharedPreferencesInitializer().setStartingList(locationId, filterType)
        val shoppingListBundle =
            DisplayPreferencesImpl().startingList(getInstrumentation().targetContext)

        assertEquals(locationId, shoppingListBundle.locationId)
        assertEquals(filterType, shoppingListBundle.filterType)
    }

    @Test
    fun getStartingList_FilterTypeIsNeeded_ReturnNeededShoppingListBundle() {
        val locationId = 7
        val filterType = FilterType.NEEDED
        SharedPreferencesInitializer().setStartingList(locationId, filterType)
        val shoppingListBundle =
            DisplayPreferencesImpl().startingList(getInstrumentation().targetContext)

        assertEquals(locationId, shoppingListBundle.locationId)
        assertEquals(filterType, shoppingListBundle.filterType)
    }

    @Test
    fun getStartingList_FilterTypeIsAll_ReturnAllShoppingListBundle() {
        val locationId = 5
        val filterType = FilterType.ALL
        SharedPreferencesInitializer().setStartingList(locationId, filterType)
        val shoppingListBundle =
            DisplayPreferencesImpl().startingList(getInstrumentation().targetContext)

        assertEquals(locationId, shoppingListBundle.locationId)
        assertEquals(filterType, shoppingListBundle.filterType)
    }

    @Test
    fun getStartingList_NoValueDefined_ReturnInStockShoppingListBundle() {
        val locationId = 1
        val filterType = FilterType.IN_STOCK
        val shoppingListBundle =
            DisplayPreferencesImpl().startingList(getInstrumentation().targetContext)

        assertEquals(locationId, shoppingListBundle.locationId)
        assertEquals(filterType, shoppingListBundle.filterType)
    }

    @Test
    fun getStartingList_InvalidIdSaved_ReturnId1() {
        val locationId = 1
        val filterType = FilterType.ALL
        SharedPreferencesInitializer().setStartingList("x|${filterType.name}")
        val shoppingListBundle =
            DisplayPreferencesImpl().startingList(getInstrumentation().targetContext)

        assertEquals(locationId, shoppingListBundle.locationId)
        assertEquals(filterType, shoppingListBundle.filterType)
    }

    @Test
    fun getStartingList_InvalidFilterTypeSaved_ReturnFilterTypeInStock() {
        val locationId = 1
        val filterType = FilterType.IN_STOCK
        SharedPreferencesInitializer().setStartingList("$locationId|x")
        val shoppingListBundle =
            DisplayPreferencesImpl().startingList(getInstrumentation().targetContext)

        assertEquals(locationId, shoppingListBundle.locationId)
        assertEquals(filterType, shoppingListBundle.filterType)
    }
}