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

package com.aisleron.ui.shop

import android.content.Context
import android.os.Bundle
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get

class ShopFragmentTest : KoinTest {
    private lateinit var bundler: Bundler
    private lateinit var addEditFragmentListener: AddEditFragmentListenerTestImpl
    private lateinit var applicationTitleUpdateListener: ApplicationTitleUpdateListenerTestImpl
    private lateinit var fabHandler: FabHandlerTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        bundler = Bundler()
        addEditFragmentListener = AddEditFragmentListenerTestImpl()
        applicationTitleUpdateListener = ApplicationTitleUpdateListenerTestImpl()
        fabHandler = FabHandlerTestImpl()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun onCreateShopFragment_HasEditBundle_AppTitleIsEdit() {
        val bundle = bundler.makeEditLocationBundle(1)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.edit_location),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShopFragment_HasEditBundle_ScreenMatchesEditLocation() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { it.pinned }
        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        getFragmentScenario(bundle)

        onView(withId(R.id.edt_shop_name)).check(matches(ViewMatchers.withText(existingShop.name)))
        onView(withId(R.id.swc_shop_pinned)).check(matches(ViewMatchers.isChecked()))
    }

    @Test
    fun onCreateShopFragment_HasAddBundle_AppTitleIsAdd() {
        val bundle = bundler.makeAddLocationBundle("New Location")
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.add_location),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onSaveClick_NewShopHasUniqueName_ShopSaved() = runTest {
        val bundle = bundler.makeAddLocationBundle("New Location")
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()
        val newShopName = "Shop Add New Test"

        onView(withId(R.id.edt_shop_name)).perform(typeText(newShopName))
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val shop = get<LocationRepository>().getAll().firstOrNull { it.name == newShopName }

        onView(withId(R.id.edt_shop_name)).check(matches(ViewMatchers.withText(newShopName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(shop)
    }

    @Test
    fun onSaveClick_NoShopNameEntered_DoNothing() {
        val bundle = bundler.makeAddLocationBundle()
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()

        scenario.onFragment {
            it.onMenuItemSelected(menuItem)
        }

        onView(withId(R.id.edt_shop_name)).check(matches(ViewMatchers.withText("")))
        Assert.assertFalse(addEditFragmentListener.addEditSuccess)
    }

    @Test
    fun onSaveClick_ExistingShopHasUniqueName_ShopUpdated() = runTest {
        val existingShop = get<LocationRepository>().getAll().first()

        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()
        val newShopName = existingShop.name + " Updated"

        onView(withId(R.id.edt_shop_name))
            .perform(ViewActions.clearText())
            .perform(typeText(newShopName))
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val updatedShop = get<LocationRepository>().get(existingShop.id)

        onView(withId(R.id.edt_shop_name)).check(matches(ViewMatchers.withText(newShopName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(updatedShop)
        Assert.assertEquals(newShopName, updatedShop?.name)
    }

    @Test
    fun onSaveClick_PinnedStatusChanged_PinnedStatusUpdated() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { !it.pinned }

        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()

        onView(withId(R.id.swc_shop_pinned)).perform(ViewActions.click())
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val updatedShop = get<LocationRepository>().get(existingShop.id)

        onView(withId(R.id.swc_shop_pinned)).check(matches(ViewMatchers.isChecked()))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertEquals(existingShop.copy(pinned = !existingShop.pinned), updatedShop)
    }

    @Test
    fun onSaveClick_IsDuplicateName_ShowErrorSnackBar() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

        val bundle = bundler.makeAddLocationBundle()
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()

        onView(withId(R.id.edt_shop_name))
            .perform(ViewActions.clearText())
            .perform(typeText(existingShop.name))
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        )
    }

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment = ShopFragment.newInstance(
            null, addEditFragmentListener, applicationTitleUpdateListener, fabHandler
        )

        Assert.assertNotNull(fragment)
    }

    private fun getSaveMenuItem(): ActionMenuItem {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val menuItem = ActionMenuItem(context, 0, R.id.mnu_btn_save, 0, 0, null)
        return menuItem
    }

    private fun getFragmentScenario(bundle: Bundle): FragmentScenario<ShopFragment> {
        val scenario = launchFragmentInContainer<ShopFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                ShopFragment(
                    addEditFragmentListener, applicationTitleUpdateListener, fabHandler
                )
            }
        )

        return scenario
    }
}