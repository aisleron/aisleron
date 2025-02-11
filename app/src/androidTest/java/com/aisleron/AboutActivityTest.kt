package com.aisleron

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import org.junit.Rule
import org.junit.Test

class AboutActivityTest {
    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule, fragmentModule, viewModelTestModule, repositoryModule, useCaseModule
        )
    )

    @Test
    fun aboutActivity_OnStart_ShowsAbout() {
        val scenario = ActivityScenario.launch(AboutActivity::class.java)
        scenario.use {
            onView(withText(R.string.about_support_header)).check(matches(isDisplayed()))
        }
    }
}