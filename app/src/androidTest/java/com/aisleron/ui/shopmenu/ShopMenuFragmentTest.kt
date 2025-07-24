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

package com.aisleron.ui.shopmenu

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get

class ShopMenuFragmentTest : KoinTest {
    private lateinit var bundler: Bundler

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    private fun getFragmentScenario(): FragmentScenario<ShopMenuFragment> {
        val scenario = launchFragmentInContainer<ShopMenuFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { ShopMenuFragment() }
        )

        scenario.onFragment {
            // Add padding to the view to account for Edge-to-Edge in Android API 35
            val rootView = it.requireView()
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
                val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(
                    view.paddingLeft,
                    systemInsets.top,
                    view.paddingRight,
                    view.paddingBottom
                )

                insets
            }

            ViewCompat.requestApplyInsets(rootView)
        }

        return scenario
    }

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment =
            ShopMenuFragment.newInstance()
        Assert.assertNotNull(fragment)
    }

    @Before
    fun setUp() {
        bundler = Bundler()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun onCreateView_ShopsLoaded_MatchesPinnedShopCount() = runTest {
        val pinnedShopCount = get<LocationRepository>().getPinnedShops().count()
        getFragmentScenario()
        onView(withId(R.id.fragment_shop_menu)).check(matches(hasChildCount(pinnedShopCount)))
    }

    @Test
    fun onItemClick_IsValidLocation_NavigateToShoppingList() = runTest {
        val shopLocation = get<LocationRepository>().getAll().first { it.pinned }

        // Create a TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        getFragmentScenario().onFragment { fragment ->
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.mobile_navigation)

            // Make the NavController available via the findNavController() APIs
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withText(shopLocation.name)).perform(click())

        val bundle = navController.currentBackStackEntry?.arguments
        val shoppingListBundle = bundler.getShoppingListBundle(bundle)

        Assert.assertEquals(shopLocation.id, shoppingListBundle.locationId)
        Assert.assertEquals(shopLocation.defaultFilter, shoppingListBundle.filterType)
        Assert.assertEquals(R.id.nav_shopping_list, navController.currentDestination?.id)
    }
}