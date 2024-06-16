package com.aisleron.ui.shoplist

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.R
import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.ui.KoinTestRule
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module


class ShopListFragmentTest {
    private lateinit var bundler: Bundler
    private lateinit var testData: TestDataManager

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    /*@get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
*/
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getKoinModules(): List<Module> {
        testData = TestDataManager()
        val testUseCases = TestUseCaseProvider(testData)
        val shopListViewModel = ShopListViewModel(
            getShopsUseCase = testUseCases.getShopsUseCase,
            getPinnedShopsUseCase = testUseCases.getPinnedShopsUseCase,
            removeLocationUseCase = testUseCases.removeLocationUseCase,
            getLocationUseCase = testUseCases.getLocationUseCase,
            TestScope(UnconfinedTestDispatcher())
        )

        /*val shoppingListViewModel = ShoppingListViewModel(
            testUseCases.getShoppingListUseCase,
            testUseCases.updateProductStatusUseCase,
            testUseCases.addAisleUseCase,
            testUseCases.updateAisleUseCase,
            testUseCases.updateAisleProductRankUseCase,
            testUseCases.updateAisleRankUseCase,
            testUseCases.removeAisleUseCase,
            testUseCases.removeProductUseCase,
            testUseCases.getAisleUseCase,
            TestScope(UnconfinedTestDispatcher())
        )*/

        return listOf(
            module {
                factory<ShopListViewModel> { shopListViewModel }
            },
/*            module {
                factory<ShoppingListViewModel> { shoppingListViewModel }
            }*/
        )
    }


    private fun getFragmentScenario(): FragmentScenario<ShopListFragment> =
        launchFragmentInContainer<ShopListFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { ShopListFragment() }
        )

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment =
            ShopListFragment.newInstance(3)
        Assert.assertNotNull(fragment)
    }

    @Before
    fun setUp() {
        bundler = Bundler()
    }

    @Test
    fun onClick_IsValidLocation_NavigateToShoppingList() {
        val editLocation = runBlocking { testData.locationRepository.getAll().first { it.id != 1 } }
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_all_shops)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withText(editLocation.name)).perform(ViewActions.click())

        val bundle = navController.backStack.last().arguments
        val shoppingListBundle = bundler.getShoppingListBundle(bundle)

        Assert.assertEquals(editLocation.id, shoppingListBundle.locationId)
        Assert.assertEquals(editLocation.defaultFilter, shoppingListBundle.filterType)
        Assert.assertEquals(R.id.nav_shopping_list, navController.currentDestination?.id)
    }

    @Test
    fun onLongClick_ActionModeNotActive_ShowActionModeContextMenu() {
        val selectedLocation =
            runBlocking { testData.locationRepository.getAll().first { it.id != 1 } }

        getFragmentScenario()
        onView(withText(selectedLocation.name)).perform(ViewActions.longClick())

        val actionBar = onView(withResourceName("action_mode_bar"))

        actionBar.check(matches(isDisplayed()))
        actionBar.check(matches(hasDescendant(withText(selectedLocation.name))))
        actionBar.check(matches(hasDescendant(withId(R.id.mnu_edit_shopping_list_item))))

        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).check(matches(isDisplayed()))
    }
}