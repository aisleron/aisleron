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
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.di.TestAppModules
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.core.module.Module


@RunWith(value = Parameterized::class)
class AboutIntentsTest(private val resourceId: Int, private val expectedUri: String) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    R.string.about_support_version_title,
                    "https://github.com/thebatdan/Aisleron/releases"
                ),
                arrayOf(
                    R.string.about_support_report_issue_title,
                    "https://github.com/thebatdan/Aisleron/issues"
                ),
                arrayOf(
                    R.string.about_support_sourcecode_title,
                    "https://github.com/thebatdan/Aisleron"
                ),
                arrayOf(
                    R.string.about_legal_license_title,
                    "https://github.com/thebatdan/Aisleron/blob/main/LICENSE"
                ),
                arrayOf(
                    R.string.about_legal_privacy_title,
                    "https://github.com/thebatdan/Aisleron/blob/main/PRIVACY.md"
                ),
                //arrayOf(R.string., "")
            )
        }
    }

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> {
        return TestAppModules().getTestAppModules(TestDataManager())
    }

    @Before
    fun setUp() {
    }

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