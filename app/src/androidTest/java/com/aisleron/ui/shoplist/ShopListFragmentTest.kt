package com.aisleron.ui.shoplist

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.R
import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.KoinTestRule
import com.aisleron.ui.bundles.AddEditLocationBundle
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.test.assertNull

class ShopListFragmentTest {
    private lateinit var bundler: Bundler
    private lateinit var testData: TestDataManager
    private lateinit var fabHandler: FabHandlerTestImpl

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

    @Before
    fun setUp() {
        bundler = Bundler()
        fabHandler = FabHandlerTestImpl()
    }

    private fun getFragmentScenario(): FragmentScenario<ShopListFragment> =
        launchFragmentInContainer<ShopListFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { ShopListFragment(fabHandler) }
        )

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment =
            ShopListFragment.newInstance(3)
        Assert.assertNotNull(fragment)
    }

    @Test
    fun onClick_IsValidLocation_NavigateToShoppingList() {
        val shopLocation = runBlocking { testData.locationRepository.getAll().first { it.id != 1 } }
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_all_shops)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withText(shopLocation.name)).perform(click())

        val bundle = navController.backStack.last().arguments
        val shoppingListBundle = bundler.getShoppingListBundle(bundle)

        assertEquals(shopLocation.id, shoppingListBundle.locationId)
        assertEquals(shopLocation.defaultFilter, shoppingListBundle.filterType)
        assertEquals(R.id.nav_shopping_list, navController.currentDestination?.id)
    }

    @Test
    fun onLongClick_ActionModeNotActive_ShowActionModeContextMenu() {
        val selectedLocation =
            runBlocking { testData.locationRepository.getAll().first { it.id != 1 } }

        getFragmentScenario()
        onView(withText(selectedLocation.name)).perform(longClick())

        val actionBar = onView(withResourceName("action_mode_bar"))

        actionBar.check(matches(isDisplayed()))
        actionBar.check(matches(hasDescendant(withText(selectedLocation.name))))
        actionBar.check(matches(hasDescendant(withId(R.id.mnu_edit_shopping_list_item))))

        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).check(matches(isDisplayed()))
    }

    @Test
    fun onActionItemClicked_ActionItemIsEdit_NavigateToEditShop() {
        val editLocation = runBlocking { testData.locationRepository.getAll().first { it.id != 1 } }
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_all_shops)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withText(editLocation.name)).perform(longClick())
        onView(withId(R.id.mnu_edit_shopping_list_item)).perform(click())

        val bundle = navController.backStack.last().arguments
        val addEditLocationBundle = bundler.getAddEditLocationBundle(bundle)

        assertEquals(editLocation.id, addEditLocationBundle.locationId)
        assertEquals(editLocation.type, addEditLocationBundle.locationType)
        assertEquals(AddEditLocationBundle.LocationAction.EDIT, addEditLocationBundle.actionType)
        assertEquals(R.id.nav_add_shop, navController.currentDestination?.id)
    }

    @Test
    fun onActionItemClicked_ActionItemIsDelete_DeleteDialogShown() {
        val deleteLocation =
            runBlocking { testData.locationRepository.getAll().first { it.id != 1 } }
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        var deleteConfirmMessage = ""

        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_all_shops)
            Navigation.setViewNavController(fragment.requireView(), navController)
            deleteConfirmMessage =
                fragment.getString(R.string.delete_confirmation, deleteLocation.name)
        }

        onView(withText(deleteLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())

        onView(withText(deleteConfirmMessage))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun onActionItemClicked_DeleteConfirmed_LocationDeleted() {
        val deleteLocation =
            runBlocking { testData.locationRepository.getAll().first { it.id != 1 } }

        getFragmentScenario()
        onView(withText(deleteLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        val deletedLocation = runBlocking { testData.locationRepository.get(deleteLocation.id) }
        Assert.assertNull(deletedLocation)
    }

    @Test
    fun onActionItemClicked_DeleteCancelled_LocationNotDeleted() {
        val deleteLocation =
            runBlocking { testData.locationRepository.getAll().first { it.id != 1 } }

        getFragmentScenario()

        onView(withText(deleteLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        val deletedLocation = runBlocking { testData.locationRepository.get(deleteLocation.id) }
        assertEquals(deleteLocation, deletedLocation)
    }

    @Test
    fun onClickFab_IsAddShopFab_NavigateToAddShop() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_all_shops)
            Navigation.setViewNavController(fragment.requireView(), navController)
            fabHandler.clickFab(FabHandler.FabOption.ADD_SHOP, fragment.requireView())
        }

        val bundle = navController.backStack.last().arguments
        val addEditShopBundle = bundler.getAddEditLocationBundle(bundle)

        assertNull(addEditShopBundle.name)
        assertEquals(AddEditLocationBundle.LocationAction.ADD, addEditShopBundle.actionType)
        assertEquals(LocationType.SHOP, addEditShopBundle.locationType)
        assertEquals(R.id.nav_add_shop, navController.currentDestination?.id)
    }
}