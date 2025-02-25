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

package com.aisleron.ui.about

import android.app.Instrumentation
import android.content.Intent
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.viewModelTestModule
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.test.KoinTest


@RunWith(value = Parameterized::class)
class AboutIntentsTest(private val resourceId: Int, private val expectedUri: String) : KoinTest {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
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
                )
            )
        }
    }

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(viewModelTestModule)
    )

    private fun getFragmentScenario(): FragmentScenario<AboutFragment> =
        launchFragmentInContainer<AboutFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { AboutFragment() }
        )

    @Test
    fun onAboutEntryClick_OnLaunchIntent_OpensCorrectUri() {
        getFragmentScenario()
        Intents.init()

        val expectedIntent = Matchers.allOf(hasAction(Intent.ACTION_VIEW), hasData(expectedUri))
        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))
        onView(withText(resourceId)).perform(click())
        intended(expectedIntent)

        Intents.release()
    }
}