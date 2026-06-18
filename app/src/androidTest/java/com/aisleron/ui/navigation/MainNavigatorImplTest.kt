/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.ui.navigation

import android.app.Activity
import android.app.Instrumentation
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.aisleron.AppCompatActivityTestImpl
import com.aisleron.ConfigActivity
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.bundles.AddEditLocationBundle
import com.aisleron.ui.bundles.AddEditProductBundle
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.navigation.IntentExtras.EXTRA_DESTINATION
import com.aisleron.ui.shoppinglist.ShoppingListGrouping
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainNavigatorImplTest {
    private lateinit var bundler: Bundler
    private lateinit var navigator: MainNavigatorImpl
    private lateinit var navController: TestNavHostController

    @get:Rule
    val activityRule = ActivityScenarioRule(AppCompatActivityTestImpl::class.java)

    @Before
    fun setUp() {
        Intents.init()

        bundler = Bundler()
        activityRule.scenario.onActivity { activity ->
            navController = TestNavHostController(activity)
            navController.setGraph(R.navigation.mobile_navigation)
            navigator = MainNavigatorImpl(bundler)
            navigator.attach(navController)
        }
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun navigateToAddShop_NavigatesToAddShop() {
        activityRule.scenario.onActivity { navigator.navigateToAddShop() }

        assertEquals(R.id.nav_add_shop, navController.currentDestination?.id)

        val bundle = navController.backStack.last().arguments
        val addEditShopBundle = bundler.getAddEditLocationBundle(bundle)
        assertEquals(AddEditLocationBundle.LocationAction.ADD, addEditShopBundle.actionType)
    }

    @Test
    fun navigateToEditShop_NavigatesToEditShop() = runTest {
        val id = 101

        activityRule.scenario.onActivity { navigator.navigateToEditShop(id) }

        assertEquals(R.id.nav_add_shop, navController.currentDestination?.id)

        val bundle = navController.backStack.last().arguments
        val addEditShopBundle = bundler.getAddEditLocationBundle(bundle)
        assertEquals(id, addEditShopBundle.locationId)
        assertEquals(AddEditLocationBundle.LocationAction.EDIT, addEditShopBundle.actionType)
    }

    @Test
    fun navigateToAddProduct_NoParameters_NavigateToAddProductWithBasicBundle() = runTest {
        val filter = FilterType.NEEDED
        val name = ""
        val aisleId: Int? = null

        activityRule.scenario.onActivity { navigator.navigateToAddProduct(filter) }

        assertEquals(R.id.nav_add_product, navController.currentDestination?.id)

        val bundle = navController.backStack.last().arguments
        val addEditProductBundle = bundler.getAddEditProductBundle(bundle)
        assertEquals(name, addEditProductBundle.name)
        assertEquals(AddEditProductBundle.ProductAction.ADD, addEditProductBundle.actionType)
        assertEquals(false, addEditProductBundle.inStock)
        assertEquals(aisleId, addEditProductBundle.aisleId)
    }

    @Test
    fun navigateToAddProduct_HasParameters_NavigateToAddProductWithFullBundle() = runTest {
        val filter = FilterType.IN_STOCK
        val name = "Test Name"
        val aisleId = 1

        activityRule.scenario.onActivity { navigator.navigateToAddProduct(filter, name, aisleId) }

        assertEquals(R.id.nav_add_product, navController.currentDestination?.id)

        val bundle = navController.backStack.last().arguments
        val addEditProductBundle = bundler.getAddEditProductBundle(bundle)
        assertEquals(name, addEditProductBundle.name)
        assertEquals(AddEditProductBundle.ProductAction.ADD, addEditProductBundle.actionType)
        assertEquals(true, addEditProductBundle.inStock)
        assertEquals(aisleId, addEditProductBundle.aisleId)
    }

    @Test
    fun navigateToEditProduct_NavigatesToEditProduct() {
        val id = 202

        activityRule.scenario.onActivity { navigator.navigateToEditProduct(id) }

        assertEquals(R.id.nav_add_product, navController.currentDestination?.id)

        val bundle = navController.backStack.last().arguments
        val addEditProductBundle = bundler.getAddEditProductBundle(bundle)
        assertEquals(id, addEditProductBundle.productId)
        assertEquals(AddEditProductBundle.ProductAction.EDIT, addEditProductBundle.actionType)
    }

    @Test
    fun navigateToAisleGroupedProductList_NavigatesToAisleGroupedProductList() {
        val id = 303
        val filter = FilterType.NEEDED

        activityRule.scenario.onActivity { navigator.navigateToAisleGroupedProductList(id, filter) }

        assertEquals(R.id.nav_shopping_list, navController.currentDestination?.id)

        val bundle = navController.backStack.last().arguments
        val shoppingListBundle = bundler.getShoppingListBundle(bundle)
        assertEquals(filter, shoppingListBundle.filterType)
        assertEquals(
            ShoppingListGrouping.AisleGrouping(id),
            shoppingListBundle.listGrouping
        )
    }

    @Test
    fun navigateToLocationGroupedProductList_NavigatesToLocationGroupedProductList() {
        val locationType = LocationType.SHOP
        val filter = FilterType.NEEDED

        activityRule.scenario.onActivity {
            navigator.navigateToLocationGroupedProductList(locationType, filter)
        }

        assertEquals(R.id.nav_shopping_list, navController.currentDestination?.id)

        val bundle = navController.backStack.last().arguments
        val shoppingListBundle = bundler.getShoppingListBundle(bundle)
        assertEquals(filter, shoppingListBundle.filterType)
        assertEquals(
            ShoppingListGrouping.LocationGrouping(locationType),
            shoppingListBundle.listGrouping
        )
    }

    @Test
    fun navigateToDefaultRoute_NavigatesToSpecifiedRoute() {
        val route1 = R.id.nav_needed
        activityRule.scenario.onActivity { navigator.navigateToDefaultRoute(route1) }
        assertEquals(route1, navController.currentDestination?.id)

        val route2 = R.id.nav_in_stock
        activityRule.scenario.onActivity { navigator.navigateToDefaultRoute(route2) }
        assertEquals(route2, navController.currentDestination?.id)
    }

    @Test
    fun navigateToWelcome_NavigatesToWelcome() {
        val route1 = R.id.nav_welcome
        activityRule.scenario.onActivity { navigator.navigateToWelcome() }
        assertEquals(route1, navController.currentDestination?.id)
    }

    @Test
    fun navigate_NavControllerIsNull_NoNavigation() {
        val nav = MainNavigatorImpl(bundler)

        nav.navigateToAddShop()
        nav.navigateToWelcome()
        nav.navigateToEditProduct(123)
    }

    @Test
    fun navigateToAbout_FiresIntentToConfigActivityWithAboutExtra() {
        val dummyResult = Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        intending(anyIntent()).respondWith(dummyResult)

        activityRule.scenario.onActivity { navigator.navigateToAbout() }

        // 2. Match that an intent was broadcast matching both the destination class and the extra bundle keys
        intended(
            allOf(
                hasComponent(ConfigActivity::class.java.name),
                hasExtra(EXTRA_DESTINATION, Destination.About)
            )
        )

    }
}