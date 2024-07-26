package com.aisleron.ui.settings

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.aisleron.MainActivity
import com.aisleron.R
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.di.TestAppModules
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module

class SettingsFragmentTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> {
        return TestAppModules().getTestAppModules(TestDataManager())
    }

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun onBackPressed_OnSettingsFragment_ReturnToMain() {
        var navController: NavController? = null
        var startDestination: NavDestination? = null
        scenario.onActivity {
            navController = it.findNavController(R.id.nav_host_fragment_content_main)
            startDestination = navController?.currentDestination
            navController?.navigate(R.id.nav_settings)
        }
        //pressBack()
        val backAction = onView(
            Matchers.allOf(withContentDescription("Navigate up"), isDisplayed())
        )
        backAction.perform(click())

        Assert.assertEquals(startDestination, navController?.currentDestination)
    }
}