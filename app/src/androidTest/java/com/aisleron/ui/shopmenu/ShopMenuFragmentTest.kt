package com.aisleron.ui.shopmenu

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
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.shoplist.ShopListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module

class ShopMenuFragmentTest {
    private lateinit var bundler: Bundler
    private lateinit var testData: TestDataManager

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

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

        return listOf(
            module {
                factory<ShopListViewModel> { shopListViewModel }
            }
        )
    }


    private fun getFragmentScenario(): FragmentScenario<ShopMenuFragment> {
        val scenario = launchFragmentInContainer<ShopMenuFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { ShopMenuFragment() }
        )

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
    }

    @Test
    fun onCreateView_ShopsLoaded_MatchesPinnedShopCount() {
        val pinnedShopCount = runBlocking { testData.locationRepository.getPinnedShops().count() }
        getFragmentScenario()
        onView(withId(R.id.fragment_shop_menu)).check(matches(hasChildCount(pinnedShopCount)))
    }

    @Test
    fun onItemClick_IsValidLocation_NavigateToShoppingList() {
        val shopLocation = runBlocking { testData.locationRepository.getAll().first { it.pinned } }

        // Create a TestNavHostController
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        getFragmentScenario().onFragment { fragment ->
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.mobile_navigation)

            // Make the NavController available via the findNavController() APIs
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withText(shopLocation.name)).perform(click())

        val bundle = navController.backStack.last().arguments
        val shoppingListBundle = bundler.getShoppingListBundle(bundle)

        Assert.assertEquals(shopLocation.id, shoppingListBundle.locationId)
        Assert.assertEquals(shopLocation.defaultFilter, shoppingListBundle.filterType)
        Assert.assertEquals(R.id.nav_shopping_list, navController.currentDestination?.id)
    }
}