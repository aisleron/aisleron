package com.aisleron

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.di.TestAppModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module

class AboutActivityTest {
    private lateinit var testData: TestDataManager

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> {
        testData = TestDataManager(false)
        return TestAppModules().getTestAppModules(testData)
    }

    @Before
    fun setUp() {
    }

    @Test
    fun aboutActivity_OnStart_ShowsAbout() {
        val scenario = ActivityScenario.launch(AboutActivity::class.java)
        scenario.use {
            onView(withText(R.string.about_support_header)).check(matches(isDisplayed()))
        }
    }
}