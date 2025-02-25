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

package com.aisleron.ui.menu

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.test.KoinTest

@RunWith(value = Parameterized::class)
class NavigationDrawerFragmentTest(
    private val testName: String,
    private val textViewId: Int,
    private val navTargetId: Int
) : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule, fragmentModule, repositoryModule, useCaseModule, viewModelTestModule
        )
    )

    private fun getFragmentScenario(): FragmentScenario<NavigationDrawerFragment> =
        launchFragmentInContainer<NavigationDrawerFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { NavigationDrawerFragment() },
            fragmentArgs = null
        )

    @Test
    fun onClick_textViewClicked_NavigateToTargetView() = runTest {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(textViewId)).perform(ViewActions.click())
        Assert.assertEquals(navTargetId, navController.currentDestination?.id)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("navInStock", R.id.nav_in_stock, R.id.nav_in_stock),
                arrayOf("navNeeded", R.id.nav_needed, R.id.nav_needed),
                arrayOf("navAllItems", R.id.nav_all_items, R.id.nav_all_items),
                arrayOf("navSettings", R.id.nav_settings, R.id.nav_settings),
                arrayOf("navAllShops", R.id.nav_all_shops, R.id.nav_all_shops)
            )
        }
    }
}