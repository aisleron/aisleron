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

package com.aisleron.ui

import android.view.View
import android.view.View.OnClickListener
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aisleron.MainActivity
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.ui.resourceprovider.ResourceProviderTestImpl
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest

class FabHandlerTest : KoinTest {
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var fabHandler: FabHandlerImpl
    private lateinit var resourceProvider: ResourceProviderTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule,
            fragmentModule,
            viewModelTestModule,
            repositoryModule,
            useCaseModule,
            generalTestModule,
            preferenceTestModule
        )
    )

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        resourceProvider = ResourceProviderTestImpl()
        resourceProvider.setOverride(R.integer.fab_rotation_duration_ms, 0)
        fabHandler = FabHandlerImpl(resourceProvider)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun clickFab_IsAddShop_NavigateToAddShop() {
        scenario.onActivity {
            fabHandler.setFabItems(it, FabHandler.FabOption.ADD_SHOP)
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
            setAllFabItems(it)
        }

        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_shop)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_aisle)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_product)).check(matches(not(isDisplayed())))
    }

    @Test
    fun clickFab_IsMultiFab_ShowAllFab() {
        scenario.onActivity {
            setAllFabItems(it)
        }

        onView(withId(R.id.fab)).perform(click())

        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_shop)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_aisle)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_product)).check(matches(isDisplayed()))

        scenario.onActivity {
            val fab = it.findViewById<FloatingActionButton>(R.id.fab)
            assertEquals(45f, fab.rotation)
        }
    }

    fun forceClick(): ViewAction {
        return object : ViewAction {

            override fun getConstraints(): Matcher<View> {
                // Constraints ensure the view is clickable and enabled
                return allOf(isClickable(), isEnabled(), isDisplayed())
            }

            override fun getDescription(): String {
                return "force click by invoking performClick()"
            }

            override fun perform(uiController: UiController, view: View) {
                // Directly call the view's internal performClick() method
                view.performClick()

                // Wait for the UI thread to stabilize after the click event
                uiController.loopMainThreadUntilIdle()
            }
        }
    }

    @Test
    fun clickFab_ChildFabShowing_HideChildFab() = runTest {
        scenario.onActivity {
            setAllFabItems(it)
        }

        val fab = onView(withId(R.id.fab))

        //Show Child Fab
        fab.perform(click())

        // Hide Child Fab. ForceClick to work around view rotation limitation in Espresso
        fab.perform(forceClick())

        fab.check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_shop)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_aisle)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_product)).check(matches(not(isDisplayed())))

        scenario.onActivity {
            val fab = it.findViewById<FloatingActionButton>(R.id.fab)
            assertEquals(0f, fab.rotation)
        }
    }

    @Test
    fun setFab_None_FabIsHidden() {
        scenario.onActivity {
            fabHandler.setFabItems(it)
        }



        onView(withId(R.id.fab)).check(matches(not(isDisplayed())))
    }

    @Test
    fun getFabView_FabVisible_ReturnsMainFab() {
        scenario.onActivity {
            setAllFabItems(it)

            val fab = fabHandler.getFabView(it)

            assertEquals(it.findViewById<FloatingActionButton>(R.id.fab), fab)
        }
    }

    private fun clickFab_ArrangeActAssert(fabOption: FabHandler.FabOption, clickMessage: String) {
        val fabOnClick = object {
            var message: String = ""
            val onClick = OnClickListener { message = clickMessage }
        }

        scenario.onActivity {
            fabHandler.setFabItems(it, fabOption)
            fabHandler.setFabOnClickListener(
                it, fabOption, fabOnClick.onClick
            )
        }

        onView(withId(R.id.fab)).perform(click())

        assertEquals(clickMessage, fabOnClick.message)
    }

    @Test
    fun clickFab_IsAddProduct_ProductOnClickTriggered() {
        val clickMessage = "Product Button Clicked"
        clickFab_ArrangeActAssert(FabHandler.FabOption.ADD_PRODUCT, clickMessage)
    }

    @Test
    fun clickFab_IsAddAisle_AisleOnClickTriggered() {
        val clickMessage = "Aisle Button Clicked"
        clickFab_ArrangeActAssert(FabHandler.FabOption.ADD_AISLE, clickMessage)
    }

    @Test
    fun clickChildFab_onClickDoesNotNavigate_resetsMainFabState() {
        // Arrange: Set up a multi-fab with a non-navigating click listener
        scenario.onActivity {
            setAllFabItems(it)
            // This listener does nothing, simulating the bug case
            fabHandler.setFabOnClickListener(it, FabHandler.FabOption.ADD_PRODUCT) { }
        }

        // Act 1: Open the main FAB menu
        onView(withId(R.id.fab)).perform(click())

        // Act 2: Click the child FAB
        onView(withId(R.id.fab_add_product)).perform(click())

        // Assert: The menu is now closed and the main FAB has been reset
        scenario.onActivity {
            val fab = it.findViewById<FloatingActionButton>(R.id.fab)
            // Check that the rotation is reset to the closed state
            assertEquals(0f, fab.rotation)
        }

        // Also assert that the child FABs are hidden again
        onView(withId(R.id.fab_add_product)).check(matches(not(isDisplayed())))
        onView(withId(R.id.fab_add_aisle)).check(matches(not(isDisplayed())))
    }

    private fun setAllFabItems(activity: MainActivity) {
        fabHandler.setFabItems(
            activity,
            FabHandler.FabOption.ADD_SHOP,
            FabHandler.FabOption.ADD_AISLE,
            FabHandler.FabOption.ADD_PRODUCT
        )
    }
}
