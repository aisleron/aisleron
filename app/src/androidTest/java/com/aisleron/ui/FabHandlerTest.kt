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

import android.view.View.OnClickListener
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import java.lang.Thread.sleep

class FabHandlerTest : KoinTest {
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var fabHandler: FabHandlerImpl

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
        fabHandler = FabHandlerImpl()
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
            fabHandler.setFabItems(
                it,
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
            fabHandler.setFabItems(
                it,
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
            fabHandler.setFabItems(
                it,
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
            fabHandler.setFabItems(it)
        }

        sleep(500)

        onView(withId(R.id.fab)).check(matches(not(isDisplayed())))
    }

    @Test
    fun getFabView_FabVisible_ReturnsMainFab() {
        scenario.onActivity {
            fabHandler.setFabItems(
                it,
                FabHandler.FabOption.ADD_SHOP,
                FabHandler.FabOption.ADD_AISLE,
                FabHandler.FabOption.ADD_PRODUCT
            )

            val fab = fabHandler.getFabView(it)

            assertEquals(it.findViewById<FloatingActionButton>(R.id.fab), fab)
        }
    }

    @Test
    fun clickFab_IsAddProduct_ProductOnClickTriggered() {

        val clickMessage = "Product Button Clicked"
        val fabOnClick = object {
            var message: String = ""
            val onClick = OnClickListener { message = clickMessage }
        }

        scenario.onActivity {
            fabHandler.setFabItems(it, FabHandler.FabOption.ADD_PRODUCT)
            fabHandler.setFabOnClickListener(
                it, FabHandler.FabOption.ADD_PRODUCT, fabOnClick.onClick
            )
        }

        onView(withId(R.id.fab)).perform(click())

        assertEquals(clickMessage, fabOnClick.message)
    }

    @Test
    fun clickFab_IsAddAisle_AisleOnClickTriggered() {

        val clickMessage = "Aisle Button Clicked"
        val fabOnClick = object {
            var message: String = ""
            val onClick = OnClickListener { message = clickMessage }
        }

        scenario.onActivity {
            fabHandler.setFabItems(it, FabHandler.FabOption.ADD_AISLE)
            fabHandler.setFabOnClickListener(
                it, FabHandler.FabOption.ADD_AISLE, fabOnClick.onClick
            )
        }

        onView(withId(R.id.fab)).perform(click())

        assertEquals(clickMessage, fabOnClick.message)
    }
}