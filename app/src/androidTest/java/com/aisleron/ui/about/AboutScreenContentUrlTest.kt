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

package com.aisleron.ui.about

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(value = Parameterized::class)
class AboutScreenContentUrlTest(private val labelResId: Int, private val expectedUri: String) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: URL={1}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    R.string.about_support_version_title,
                    "https://aisleron.com/docs/version-history"
                ),
                arrayOf(
                    R.string.about_support_report_issue_title,
                    "https://aisleron.com/docs/reporting-issues"
                ),
                arrayOf(
                    R.string.about_support_sourcecode_title,
                    "https://github.com/aisleron/aisleron"
                ),
                arrayOf(
                    R.string.about_legal_license_title,
                    "https://aisleron.com/docs/licenses-policies/aisleron-license"
                ),
                arrayOf(
                    R.string.about_legal_privacy_title,
                    "https://aisleron.com/docs/licenses-policies/aisleron-privacy-policy"
                ),
                arrayOf(
                    R.string.about_support_documentation_title,
                    "https://aisleron.com/docs/documentation/"
                ),
                arrayOf(
                    R.string.about_legal_3rdparty_title,
                    "https://aisleron.com/docs/licenses-policies/3rd-party-licenses"
                ),
                arrayOf(
                    R.string.about_contribute_translate_title,
                    "https://aisleron.com/docs/contribute/translate"
                ),
                arrayOf(
                    R.string.about_contribute_financial_title,
                    "https://aisleron.com/docs/contribute/financial_contributions"
                )
            )
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onAboutEntryClick_InvokesCallbackWithCorrectUrl() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedLabelString = context.getString(labelResId)
        var capturedUrl: String? = null

        // 2. Launch the stateless UI component in isolation
        composeTestRule.setContent {
            AboutScreenContent(
                versionName = "2.4.1",
                onBackPressed = {},
                onUrlClick = { url -> capturedUrl = url } // Trap the URL string in our test hook
            )
        }

        // 3. Find, Scroll to, and Click the item using semantic text matchers
        composeTestRule.onNodeWithText(expectedLabelString)
            .performScrollTo()
            .performClick()

        // 4. Verify the UI passed the correct string payload back up to the parent layer
        assertEquals(expectedUri, capturedUrl)
    }
}