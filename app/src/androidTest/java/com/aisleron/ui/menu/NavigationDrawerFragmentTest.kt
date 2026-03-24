/*
 * Copyright (C) 2025-2026 aisleron.com
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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.ui.navigation.Navigator
import com.aisleron.ui.navigation.NavigatorTestImpl
import com.aisleron.ui.shopmenu.ShopMenuFragment
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.test.KoinTest
import org.koin.test.get

@RunWith(value = Parameterized::class)
class NavigationDrawerFragmentTest(
    private val testName: String,
    private val textViewId: Int,
    private val navTargetId: Int
) : KoinTest {
    private lateinit var navigator: NavigatorTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule, repositoryModule, useCaseModule, viewModelTestModule, generalTestModule
        )
    )

    @Before
    fun setUp() {
        navigator = get<Navigator>() as NavigatorTestImpl
    }

    private fun getFragmentScenario(): FragmentScenario<NavigationDrawerFragment> {
        val testFactory = object : androidx.fragment.app.FragmentFactory() {
            override fun instantiate(
                classLoader: ClassLoader, className: String
            ): androidx.fragment.app.Fragment {
                return when (className) {
                    NavigationDrawerFragment::class.java.name -> NavigationDrawerFragment(navigator)
                    ShopMenuFragment::class.java.name -> ShopMenuFragment(navigator)

                    else -> super.instantiate(classLoader, className)
                }
            }
        }


        return launchFragmentInContainer<NavigationDrawerFragment>(
            themeResId = R.style.Theme_Aisleron,
            factory = testFactory,
            fragmentArgs = null
        )
    }

    @Test
    fun onClick_textViewClicked_NavigateToTargetView() = runTest {
        getFragmentScenario()

        onView(withId(textViewId)).perform(ViewActions.click())

        Assert.assertEquals(navTargetId, navigator.destination)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("navInStock", R.id.nav_in_stock, R.id.nav_in_stock),
                arrayOf("navNeeded", R.id.nav_needed, R.id.nav_needed),
                arrayOf("navAllItems", R.id.nav_all_items, R.id.nav_all_items),
                arrayOf("navAllShops", R.id.nav_all_shops, R.id.nav_shopping_list),
                arrayOf("navSettings", R.id.nav_settings, R.id.nav_settings),
                arrayOf("navAllLists", R.id.nav_all_lists, R.id.nav_all_lists)
            )
        }
    }
}