/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.aisleron.di.KoinTestRule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.ui.navigation.Destination
import com.aisleron.ui.navigation.IntentExtras.EXTRA_DESTINATION
import org.junit.Rule
import org.junit.Test


class ConfigActivityTest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            preferenceTestModule, viewModelTestModule
        )
    )

    @Test
    fun configActivity_NoBundleProvided_DefaultsToAboutScreen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, ConfigActivity::class.java)

        ActivityScenario.launch<ConfigActivity>(intent).use {
            val expectedText = context.getString(R.string.about_support_header)

            composeTestRule
                .onNodeWithText(expectedText)
                .assertIsDisplayed()
        }
    }

    @Test
    fun configActivity_AboutDestinationProvided_ShowsAboutScreen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, ConfigActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION, Destination.About)
        }

        ActivityScenario.launch<ConfigActivity>(intent).use {
            val expectedText = context.getString(R.string.about_support_header)

            composeTestRule
                .onNodeWithText(expectedText)
                .assertIsDisplayed()
        }
    }
}