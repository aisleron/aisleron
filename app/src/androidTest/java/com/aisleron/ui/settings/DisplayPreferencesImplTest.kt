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
import com.aisleron.domain.preferences.ApplicationTheme
import com.aisleron.domain.preferences.PureBlackStyle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DisplayPreferencesImplTest {
    private lateinit var displayPreferences: DisplayPreferences
    private lateinit var sharedPreferencesInitializer: SharedPreferencesInitializer

    @Before
    fun setUp() {
        sharedPreferencesInitializer = SharedPreferencesInitializer()
        displayPreferences = DisplayPreferencesImpl(getInstrumentation().targetContext)
        sharedPreferencesInitializer.clearPreferences()
    }

    @Test
    fun getApplicationTheme_returnsCorrectEnum_forGivenSetting() {
        val testCases = mapOf(
            SharedPreferencesInitializer.ApplicationTheme.LIGHT_THEME to ApplicationTheme.LIGHT_THEME,
            SharedPreferencesInitializer.ApplicationTheme.DARK_THEME to ApplicationTheme.DARK_THEME,
            SharedPreferencesInitializer.ApplicationTheme.SYSTEM_THEME to ApplicationTheme.SYSTEM_THEME
        )

        testCases.forEach { (inputValue, expectedValue) ->
            sharedPreferencesInitializer.setApplicationTheme(inputValue)
            val actual = displayPreferences.applicationTheme()
            assertEquals(expectedValue, actual, "Failed for theme: ${inputValue.name}")
        }
    }

    @Test
    fun showOnLockScreen_returnsCorrectBoolean_forGivenSetting() {
        listOf(true, false).forEach { showOnLockScreen ->
            sharedPreferencesInitializer.setShowOnLockScreen(showOnLockScreen)
            val actual = displayPreferences.showOnLockScreen()
            assertEquals(showOnLockScreen, actual, "Failed for showOnLockScreen: $showOnLockScreen")
        }
    }

    @Test
    fun getStartingList_returnsCorrectBundle_forGivenSetting() {
        data class TestCase(
            val description: String,
            val setup: () -> Unit,
            val expectedLocationId: Int,
            val expectedFilterType: FilterType
        )

        val testCases = listOf(
            TestCase(
                description = "In-Stock filter",
                setup = { sharedPreferencesInitializer.setStartingList(1, FilterType.IN_STOCK) },
                expectedLocationId = 1,
                expectedFilterType = FilterType.IN_STOCK
            ),
            TestCase(
                description = "Needed filter",
                setup = { sharedPreferencesInitializer.setStartingList(7, FilterType.NEEDED) },
                expectedLocationId = 7,
                expectedFilterType = FilterType.NEEDED
            ),
            TestCase(
                description = "All filter",
                setup = { sharedPreferencesInitializer.setStartingList(5, FilterType.ALL) },
                expectedLocationId = 5,
                expectedFilterType = FilterType.ALL
            ),
            TestCase(
                description = "No value defined, should default",
                setup = { /* no setup */ },
                expectedLocationId = 1,
                expectedFilterType = FilterType.IN_STOCK
            ),
            TestCase(
                description = "Invalid ID saved, should default ID",
                setup = { sharedPreferencesInitializer.setStartingList("x|${FilterType.ALL.name}") },
                expectedLocationId = 1,
                expectedFilterType = FilterType.ALL
            ),
            TestCase(
                description = "Invalid filter type, should default filter",
                setup = { sharedPreferencesInitializer.setStartingList("1|x") },
                expectedLocationId = 1,
                expectedFilterType = FilterType.IN_STOCK
            )
        )

        testCases.forEach { case ->
            sharedPreferencesInitializer.clearPreferences() // Reset for each case
            case.setup()

            val shoppingListBundle = displayPreferences.startingList()

            assertEquals(
                case.expectedLocationId,
                shoppingListBundle.locationId,
                "Failed id for: ${case.description}"
            )

            assertEquals(
                case.expectedFilterType,
                shoppingListBundle.filterType,
                "Failed filter for: ${case.description}"
            )
        }
    }

    @Test
    fun dynamicColor_returnsCorrectBoolean_forGivenSetting() {
        listOf(true, false).forEach { dynamicColor ->
            sharedPreferencesInitializer.setDynamicColor(dynamicColor)
            val actual = displayPreferences.dynamicColor()
            assertEquals(dynamicColor, actual, "Failed for dynamicColor: $dynamicColor")
        }
    }

    @Test
    fun pureBlackStyle_returnsCorrectEnum_forGivenSetting() {
        val testCases = mapOf(
            SharedPreferencesInitializer.PureBlackStyle.DEFAULT to PureBlackStyle.DEFAULT,
            SharedPreferencesInitializer.PureBlackStyle.ECONOMY to PureBlackStyle.ECONOMY,
            SharedPreferencesInitializer.PureBlackStyle.BUSINESS_CLASS to PureBlackStyle.BUSINESS_CLASS,
            SharedPreferencesInitializer.PureBlackStyle.FIRST_CLASS to PureBlackStyle.FIRST_CLASS
        )

        testCases.forEach { (inputValue, expectedValue) ->
            sharedPreferencesInitializer.setPureBlackStyle(inputValue)
            val actual = displayPreferences.pureBlackStyle()
            assertEquals(expectedValue, actual, "Failed for style: ${inputValue.name}")
        }
    }
}