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

package com.aisleron.ui.component.preference

import androidx.compose.material3.Switch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PreferenceTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTitle = "Test Preference Title"
    private val testSummary = "Test Preference Summary"
    private val controlTag = "test_control_tag"
    private val iconDescRes = R.string.about_support_documentation_title

    @Test
    fun preference_MinimalSetup_RendersTitleAndHidesOptionalComponents() {
        composeTestRule.setContent {
            Preference(
                title = testTitle,
                summary = null,
                onClick = null,
                iconResId = null,
                control = null
            )
        }

        // Title must show
        composeTestRule.onNodeWithText(testTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(testTitle).assertHasNoClickAction()

        // Optional components must not exist in the layout tree
        composeTestRule.onNodeWithText(testSummary).assertDoesNotExist()
        composeTestRule.onNodeWithTag(controlTag).assertDoesNotExist()
    }

    @Test
    fun preference_WithSummaryProvided_RendersSummaryText() {
        composeTestRule.setContent {
            Preference(
                title = testTitle,
                summary = testSummary
            )
        }

        composeTestRule.onNodeWithText(testSummary).assertIsDisplayed()
    }

    @Test
    fun preference_WithIconAndContentDescription_RendersIconComponentWithDescription() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedContentDescription = context.getString(iconDescRes)

        composeTestRule.setContent {
            Preference(
                title = testTitle,
                iconResId = R.drawable.baseline_add_aisle_24,
                iconContentDescriptionResId = iconDescRes
            )
        }

        composeTestRule.onNodeWithContentDescription(expectedContentDescription).assertIsDisplayed()
    }

    @Test
    fun preference_WithControlSlotProvided_AttachesWidgetInsideSlot() {
        composeTestRule.setContent {
            Preference(
                title = testTitle,
                control = {
                    Switch(
                        checked = false,
                        onCheckedChange = {},
                        modifier = Modifier.testTag(controlTag)
                    )
                }
            )
        }

        composeTestRule.onNodeWithTag(controlTag).assertIsDisplayed()
    }

    private fun preference_WithOnClickAction_ArrangeActAssert(enabled: Boolean) {
        var clickCount = 0

        composeTestRule.setContent {
            Preference(
                title = testTitle,
                enabled = enabled,
                onClick = { clickCount++ }
            )
        }

        val rowNode = composeTestRule.onNodeWithText(testTitle)
        rowNode.assertHasClickAction()
        if (enabled) rowNode.assertIsEnabled() else rowNode.assertIsNotEnabled()
        rowNode.performClick()

        val expectedCount = if (enabled) 1 else 0
        assertEquals(expectedCount, clickCount)
    }

    @Test
    fun preference_WithOnClickActionAndEnabled_RegistersClickEvents() {
        preference_WithOnClickAction_ArrangeActAssert(true)
    }

    @Test
    fun preference_WithOnClickActionAndDisabled_BlocksClickEvents() {
        preference_WithOnClickAction_ArrangeActAssert(false)
    }
}