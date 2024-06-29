package com.aisleron.ui

import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aisleron.MainActivity
import com.aisleron.R
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.di.TestAppModules
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import java.lang.Thread.sleep

class FabHandlerImplTest {
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
    fun clickFab_IsAddShop_NavigateToAddShop() {
        scenario.onActivity {
            val fabHandler = FabHandlerImpl(it)
            fabHandler.setFabItems(FabHandler.FabOption.ADD_SHOP)
        }

        onView(withId(R.id.fab)).perform(click())

        scenario.onActivity {
            val navController = it.findNavController(R.id.nav_host_fragment_content_main)
            assertEquals(R.id.nav_add_shop, navController.currentDestination?.id)
        }
    }

    @Test
    fun loadedMultiFab_FabNotClicked_ChildFabHidden() {
        scenario.onActivity {
            val fabHandler = FabHandlerImpl(it)
            fabHandler.setFabItems(
                FabHandler.FabOption.ADD_SHOP,
                FabHandler.FabOption.ADD_AISLE,
                FabHandler.FabOption.ADD_PRODUCT
            )
        }

        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_shop)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_aisle)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_product)).check(matches(not(isDisplayed())))
    }

    @Test
    fun clickFab_IsMultiFab_ShopAllFab() {
        scenario.onActivity {
            val fabHandler = FabHandlerImpl(it)
            fabHandler.setFabItems(
                FabHandler.FabOption.ADD_SHOP,
                FabHandler.FabOption.ADD_AISLE,
                FabHandler.FabOption.ADD_PRODUCT
            )
        }

        onView(withId(R.id.fab)).perform(click())

        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_shop)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_aisle)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_product)).check(matches(isDisplayed()))
    }

    @Test
    fun clickFab_ChildFabShowing_HideChildFab() {
        scenario.onActivity {
            val fabHandler = FabHandlerImpl(it)
            fabHandler.setFabItems(
                FabHandler.FabOption.ADD_SHOP,
                FabHandler.FabOption.ADD_AISLE,
                FabHandler.FabOption.ADD_PRODUCT
            )
        }

        onView(withId(R.id.fab)).perform(click())  //Show Child Fab
        onView(withId(R.id.fab)).perform(click())  //Hide Child Fab

        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_shop)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_aisle)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_product)).check(matches(not(isDisplayed())))
    }

    @Test
    fun setFab_None_FabIsHidden() {
        scenario.onActivity {
            val fabHandler = FabHandlerImpl(it)
            fabHandler.setFabItems()
        }

        sleep(500)

        onView(withId(R.id.fab)).check(matches(not(isDisplayed())))
    }
}