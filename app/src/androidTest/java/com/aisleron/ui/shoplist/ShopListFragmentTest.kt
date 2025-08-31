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

package com.aisleron.ui.shoplist

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.AppCompatActivityTestImpl
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.AddEditLocationBundle
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare

class ShopListFragmentTest : KoinTest {
    private lateinit var bundler: Bundler
    private lateinit var fabHandler: FabHandlerTestImpl
    private lateinit var activityFragment: ShopListFragment

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        bundler = Bundler()
        fabHandler = FabHandlerTestImpl()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private fun setPadding(view: View) {
        ViewCompat.getRootWindowInsets(view)?.let { windowInsets ->
            val actionBarHeight = view.resources.getDimensionPixelSize(R.dimen.toolbar_height)
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = actionBarHeight + insets.top)
        }
    }

    private fun getActivityScenario(): ActivityScenario<AppCompatActivityTestImpl> {
        val scenario = ActivityScenario.launch(AppCompatActivityTestImpl::class.java)
        scenario.onActivity { activity ->
            activityFragment = ShopListFragment(fabHandler)
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, activityFragment, "SHOP_LIST")
                .commitNow()

            setPadding(activityFragment.requireView())
        }

        return scenario
    }

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment =
            ShopListFragment.newInstance(3, fabHandler)
        Assert.assertNotNull(fragment)
    }

    @Test
    fun onClick_IsValidLocation_NavigateToShoppingList() = runTest {
        val shopLocation = get<LocationRepository>().getAll().first { it.id != 1 }
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        getActivityScenario().onActivity {
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_all_shops)
            Navigation.setViewNavController(activityFragment.requireView(), navController)
        }

        onView(withText(shopLocation.name)).perform(click())

        val bundle = navController.backStack.last().arguments
        val shoppingListBundle = bundler.getShoppingListBundle(bundle)

        assertEquals(shopLocation.id, shoppingListBundle.locationId)
        assertEquals(shopLocation.defaultFilter, shoppingListBundle.filterType)
        assertEquals(R.id.nav_shopping_list, navController.currentDestination?.id)
    }

    @Test
    fun onLongClick_ActionModeNotActive_ShowActionModeContextMenu() = runTest {
        val selectedLocation = get<LocationRepository>().getAll().first { it.id != 1 }

        getActivityScenario()
        val shopItem = onView(allOf(withText(selectedLocation.name), withId(R.id.txt_shop_name)))
        shopItem.perform(longClick())

        shopItem.check(matches(isSelected()))

        val actionBar = onView(withId(com.google.android.material.R.id.action_context_bar))

        actionBar.check(matches(isDisplayed()))
        actionBar.check(matches(hasDescendant(withText(selectedLocation.name))))
        actionBar.check(matches(hasDescendant(withId(R.id.mnu_edit_shop_list_item))))

        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).check(matches(isDisplayed()))
    }

    @Test
    fun onActionItemClicked_ActionItemIsEdit_NavigateToEditShop() = runTest {
        val editLocation = get<LocationRepository>().getAll().first { it.id != 1 }
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        getActivityScenario().onActivity {
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_all_shops)
            Navigation.setViewNavController(activityFragment.requireView(), navController)
        }

        onView(withText(editLocation.name)).perform(longClick())
        onView(withId(R.id.mnu_edit_shop_list_item)).perform(click())

        val bundle = navController.backStack.last().arguments
        val addEditLocationBundle = bundler.getAddEditLocationBundle(bundle)

        assertEquals(editLocation.id, addEditLocationBundle.locationId)
        assertEquals(editLocation.type, addEditLocationBundle.locationType)
        assertEquals(AddEditLocationBundle.LocationAction.EDIT, addEditLocationBundle.actionType)
        assertEquals(R.id.nav_add_shop, navController.currentDestination?.id)
    }

    @Test
    fun onActionItemClicked_ActionItemIsDelete_DeleteDialogShown() = runTest {
        val deleteLocation = get<LocationRepository>().getAll().first { it.id != 1 }
        var deleteConfirmMessage = ""

        getActivityScenario().onActivity {
            deleteConfirmMessage =
                activityFragment.getString(R.string.delete_confirmation, deleteLocation.name)
        }

        onView(withText(deleteLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())

        onView(withText(deleteConfirmMessage))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun onActionItemClicked_DeleteConfirmed_LocationDeleted() = runTest {
        val locationRepository = get<LocationRepository>()
        val deleteLocation = locationRepository.getAll().first { it.id != 1 }

        getActivityScenario()
        onView(withText(deleteLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .perform(click())

        val deletedLocation = locationRepository.get(deleteLocation.id)
        Assert.assertNull(deletedLocation)
    }

    @Test
    fun onActionItemClicked_DeleteCancelled_LocationNotDeleted() = runTest {
        val locationRepository = get<LocationRepository>()
        val deleteLocation = locationRepository.getAll().first { it.id != 1 }

        getActivityScenario()

        onView(withText(deleteLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        val deletedLocation = locationRepository.get(deleteLocation.id)
        assertEquals(deleteLocation, deletedLocation)
    }

    @Test
    fun onViewModelStateChange_IsError_ShowErrorSnackBar() = runTest {
        val exceptionMessage = "Error on delete Shop"

        declare<RemoveLocationUseCase> {
            object : RemoveLocationUseCase {
                override suspend fun invoke(location: Location) {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val locationRepository = get<LocationRepository>()
        val deleteLocation = locationRepository.getAll().first { it.id != 1 }

        getActivityScenario()
        onView(withText(deleteLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                allOf(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                    withText(startsWith("ERROR:"))
                )
            )
        )
    }

    @Test
    fun onClick_ActionModeIsActive_DismissActionModeContextMenu() = runTest {
        val selectedLocation = get<LocationRepository>().getAll().first { it.id != 1 }
        getActivityScenario()
        val shopItem = onView(allOf(withText(selectedLocation.name), withId(R.id.txt_shop_name)))

        shopItem.perform(longClick())
        shopItem.perform(click())

        val actionBar = onView(withId(com.google.android.material.R.id.action_context_bar))
        actionBar.checkVisibility(View.GONE)
    }

    @Test
    fun onClickAddShopFab_ActionModeIsActive_DismissActionModeContextMenu() {
        runTest {
            val selectedLocation = get<LocationRepository>().getAll().first { it.id != 1 }
            val shopItem =
                onView(allOf(withText(selectedLocation.name), withId(R.id.txt_shop_name)))
            val scenario = getActivityScenario()

            shopItem.perform(longClick())

            scenario.onActivity { activity ->
                fabHandler.setFabOnClickListener(
                    activity, FabHandler.FabOption.ADD_SHOP
                ) {}

                fabHandler.clickFab(FabHandler.FabOption.ADD_SHOP, activityFragment.requireView())
            }

            val actionBar = onView(withId(com.google.android.material.R.id.action_context_bar))
            actionBar.checkVisibility(View.GONE)
        }
    }

    @Test
    fun onActionItemClicked_ActionItemIsCopy_CopyDialogShown() = runTest {
        val copyLocation = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        var copyDialogTitle = ""

        getActivityScenario().onActivity {
            copyDialogTitle =
                activityFragment.getString(R.string.copy_entity_title, copyLocation.name)
        }

        onView(withText(copyLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(android.R.string.copy)).perform(click())

        onView(withText(copyDialogTitle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun onActionItemClicked_CopyConfirmed_ConfirmSnackbarShown() = runTest {
        val copyLocation = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        var copyDialogTitle = ""
        var confirmCopy = ""

        getActivityScenario().onActivity {
            confirmCopy = activityFragment.getString(R.string.entity_copied, copyLocation.name)
            copyDialogTitle =
                activityFragment.getString(R.string.copy_entity_title, copyLocation.name)
        }

        onView(withText(copyLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(android.R.string.copy)).perform(click())

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .perform(click())

        onView(withText(copyDialogTitle))
            .check(doesNotExist())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                allOf(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                    withText(confirmCopy)
                )
            )
        )
    }

    @Test
    fun onActionItemClicked_CopyCancelled_DialogClosed() = runTest {
        val copyLocation = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        var copyDialogTitle = ""

        getActivityScenario().onActivity {
            copyDialogTitle =
                activityFragment.getString(R.string.copy_entity_title, copyLocation.name)
        }

        onView(withText(copyLocation.name)).perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(android.R.string.copy)).perform(click())

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        onView(withText(copyDialogTitle))
            .check(doesNotExist())
    }

    private fun ViewInteraction.checkVisibility(
        expectedVisibility: Int,
        timeoutMs: Long = 2000,
        pollIntervalMs: Long = 50
    ) {
        this.perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isAssignableFrom(View::class.java)

            override fun getDescription(): String =
                "wait up to $timeoutMs ms for view to have visibility: $expectedVisibility"

            override fun perform(uiController: UiController, view: View) {
                val endTime = System.currentTimeMillis() + timeoutMs
                do {
                    if (view.visibility == expectedVisibility) return
                    uiController.loopMainThreadForAtLeast(pollIntervalMs)
                } while (System.currentTimeMillis() < endTime)

                throw AssertionError("View did not become visibility=$expectedVisibility within $timeoutMs ms")
            }
        })
    }
}