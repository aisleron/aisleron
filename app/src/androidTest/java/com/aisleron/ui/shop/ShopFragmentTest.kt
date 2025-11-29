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
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.loyaltycard.LoyaltyCardProvider
import com.aisleron.ui.loyaltycard.LoyaltyCardProviderTestImpl
import com.aisleron.ui.settings.ShopPreferencesTestImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers.emptyString
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
    fun onCreateShopFragment_HasEditBundle_AppTitleIsEdit() = runTest {
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

        onView(withId(R.id.edt_shop_name)).check(matches(withText(existingShop.name)))
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
        val newShopName = "Shop Add New Test"

        onView(withId(R.id.edt_shop_name)).perform(typeText(newShopName))
        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val shop = get<LocationRepository>().getAll().firstOrNull { it.name == newShopName }

        onView(withId(R.id.edt_shop_name)).check(matches(withText(newShopName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(shop)
    }

    @Test
    fun onSaveClick_NoShopNameEntered_DoNothing() = runTest {
        val bundle = bundler.makeAddLocationBundle()
        val scenario = getFragmentScenario(bundle)

        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        onView(withId(R.id.edt_shop_name)).check(matches(withText("")))
        Assert.assertFalse(addEditFragmentListener.addEditSuccess)
    }

    @Test
    fun onSaveClick_ExistingShopHasUniqueName_ShopUpdated() = runTest {
        val existingShop = get<LocationRepository>().getAll().first()
        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val scenario = getFragmentScenario(bundle)
        val newShopName = existingShop.name + " Updated"

        onView(withId(R.id.edt_shop_name))
            .perform(clearText())
            .perform(typeText(newShopName))

        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val updatedShop = get<LocationRepository>().get(existingShop.id)

        onView(withId(R.id.edt_shop_name)).check(matches(withText(newShopName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(updatedShop)
        Assert.assertEquals(newShopName, updatedShop?.name)
    }

    @Test
    fun onSaveClick_PinnedStatusChanged_PinnedStatusUpdated() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { !it.pinned }
        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val scenario = getFragmentScenario(bundle)

        onView(withId(R.id.swc_shop_pinned)).perform(ViewActions.click())
        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

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

        onView(withId(R.id.edt_shop_name))
            .perform(clearText())
            .perform(typeText(existingShop.name))
        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

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
            null,
            addEditFragmentListener,
            applicationTitleUpdateListener,
            LoyaltyCardProviderTestImpl(),
            ShopPreferencesTestImpl()
        )

        Assert.assertNotNull(fragment)
    }

    private fun getSaveMenuItem(context: Context): ActionMenuItem {
        val menuItem = ActionMenuItem(context, 0, R.id.mnu_btn_save, 0, 0, null)
        return menuItem
    }

    private fun getFragmentScenario(
        bundle: Bundle,
        loyaltyCardProvider: LoyaltyCardProvider? = null,
        shopPreferences: ShopPreferencesTestImpl? = null

    ): FragmentScenario<ShopFragment> {
        val scenario = launchFragmentInContainer<ShopFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                ShopFragment(
                    addEditFragmentListener,
                    applicationTitleUpdateListener,
                    loyaltyCardProvider ?: LoyaltyCardProviderTestImpl(),
                    shopPreferences ?: ShopPreferencesTestImpl()
                )
            }
        )

        return scenario
    }

    @Test
    fun onLookupLoyaltyCardClick_ProviderReturnsLoyaltyCard_LoyaltyCardNameDisplayed() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val loyaltyCardProvider =
            LoyaltyCardProviderTestImpl(loyaltyCardName = "Test Loyalty Card Lookup")

        getFragmentScenario(bundle, loyaltyCardProvider)

        onView(withId(R.id.btn_lookup_loyalty_card)).perform(ViewActions.click())

        onView(withId(R.id.edt_shop_loyalty_card)).check(matches(withText("Test Loyalty Card Lookup")))
    }

    private suspend fun getLoyaltyCard(): LoyaltyCard {
        val loyaltyCard = LoyaltyCard(
            id = 0,
            name = "Loyalty Card Test",
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Dummy Intent"
        )

        val loyaltyCardId = get<LoyaltyCardRepository>().add(loyaltyCard)

        return loyaltyCard.copy(id = loyaltyCardId)
    }

    @Test
    fun onCreateShopFragment_LocationHasLoyaltyCard_LoyaltyCardDisplayed() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val loyaltyCard = getLoyaltyCard()
        get<LoyaltyCardRepository>().addToLocation(existingShop.id, loyaltyCard.id)
        val bundle = bundler.makeEditLocationBundle(existingShop.id)

        getFragmentScenario(bundle)

        onView(withId(R.id.edt_shop_loyalty_card)).check(matches(withText(loyaltyCard.name)))
    }

    @Test
    fun onRemoveLoyaltyCardClick_LocationHAsLoyaltyCard_LoyaltyCardNameCleared() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val loyaltyCard = getLoyaltyCard()
        get<LoyaltyCardRepository>().addToLocation(existingShop.id, loyaltyCard.id)
        val bundle = bundler.makeEditLocationBundle(existingShop.id)

        getFragmentScenario(bundle)

        onView(withId(R.id.btn_delete_loyalty_card)).perform(ViewActions.click())

        onView(withId(R.id.edt_shop_loyalty_card)).check(matches(withText("")))
    }

    @Test
    fun onLookupLoyaltyCardClick_ProviderNotInstalled_ShowNotInstalledDialog() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val loyaltyCardProvider = LoyaltyCardProviderTestImpl(throwNotInstalledException = true)
        getFragmentScenario(bundle, loyaltyCardProvider)

        onView(withId(R.id.btn_lookup_loyalty_card)).perform(ViewActions.click())

        onView(withText(R.string.loyalty_card_provider_missing_title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun onLookupLoyaltyCardClick_ProviderGenericError_ShowErrorSnackBar() = runTest {
        val existingShop = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val loyaltyCardProvider = LoyaltyCardProviderTestImpl(throwGenericException = true)
        getFragmentScenario(bundle, loyaltyCardProvider)

        onView(withId(R.id.btn_lookup_loyalty_card)).perform(ViewActions.click())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        )
    }

    @Test
    fun onRotateDevice_ShopDetailsChanged_ShopDetailsPersist() = runTest {
        val bundle = bundler.makeAddLocationBundle("New Location")
        val newLocationName = "Location Add New Test"
        getFragmentScenario(bundle)

        onView(withId(R.id.edt_shop_name)).perform(typeText(newLocationName))

        val device = UiDevice.getInstance(getInstrumentation())

        try {
            device.setOrientationLandscape()

            onView(withId(R.id.edt_shop_name)).check(matches(withText(newLocationName)))
        } finally {
            device.setOrientationPortrait()
        }
    }

    private fun getShowExtraOptionsPreference(showExtraOptions: Boolean): ShopPreferencesTestImpl {
        val preferences = ShopPreferencesTestImpl()
        preferences.setShowExtraOptions(getInstrumentation().targetContext, showExtraOptions)
        return preferences
    }

    private suspend fun getShop(): Location {
        return get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
    }

    @Test
    fun onCreateView_PreferenceIsHideExtraOption_ExtraOptionsGone() = runTest {
        val preferences = getShowExtraOptionsPreference(false)
        val existingShop = getShop()
        val bundle = bundler.makeEditLocationBundle(existingShop.id)

        getFragmentScenario(bundle, null, preferences)

        onView(withId(R.id.txt_toggle_extra_options))
            .check(matches(withText(R.string.extra_options)))

        onView(withId(R.id.layout_extra_options)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)
            )
        )
    }

    @Test
    fun onCreateView_PreferenceIsShowExtraOption_ExtraOptionsVisible() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val existingShop = getShop()
        val bundle = bundler.makeEditLocationBundle(existingShop.id)

        getFragmentScenario(bundle, null, preferences)

        onView(withId(R.id.layout_extra_options)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        )
    }

    @Test
    fun onClickExtrasToggle_ExtraOptionsIsGone_ExtraOptionsVisible() = runTest {
        val preferences = getShowExtraOptionsPreference(false)
        val existingShop = getShop()
        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        getFragmentScenario(bundle, null, preferences)

        onView(withId(R.id.txt_toggle_extra_options))
            .perform(ViewActions.click())

        onView(withId(R.id.layout_extra_options)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        )

        val endPreference = preferences.showExtraOptions(getInstrumentation().targetContext)
        assertTrue(endPreference)
    }

    @Test
    fun onClickExtrasToggle_ExtraOptionsIsVisible_ExtraOptionsGone() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val existingShop = getShop()
        val bundle = bundler.makeEditLocationBundle(existingShop.id)

        getFragmentScenario(bundle, null, preferences)
        onView(withId(R.id.txt_toggle_extra_options))
            .perform(ViewActions.click())

        onView(withId(R.id.layout_extra_options)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)
            )
        )

        val endPreference = preferences.showExtraOptions(getInstrumentation().targetContext)
        assertFalse(endPreference)
    }

    @Test
    fun onCreateView_ShopHasNoNote_NoteEmpty() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val existingShop = getShop()
        val bundle = bundler.makeEditLocationBundle(existingShop.id)

        getFragmentScenario(bundle, null, preferences)
        onView(
            allOf(
                isDescendantOfA(withId(R.id.tab_shop_options)),
                withText(R.string.tab_notes)
            )
        ).perform(ViewActions.click())

        onView(withId(R.id.edt_notes)).check(
            matches(withText(emptyString()))
        )
    }

    @Test
    fun onCreateView_ShopHasNote_NoteDisplayed() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val locationRepository = get<LocationRepository>()
        val existingShop = getShop()
        val noteText = "Test note displayed on shop"
        val noteId = get<NoteRepository>().add(Note(0, noteText))
        locationRepository.update(existingShop.copy(noteId = noteId))
        val bundle = bundler.makeEditLocationBundle(existingShop.id)

        getFragmentScenario(bundle, null, preferences)
        onView(
            allOf(
                isDescendantOfA(withId(R.id.tab_shop_options)),
                withText(R.string.tab_notes)
            )
        ).perform(ViewActions.click())

        onView(withId(R.id.edt_notes)).check(
            matches(withText(noteText))
        )
    }

    @Test
    fun onSaveClick_NoteEntered_NoteSaved() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val locationRepository = get<LocationRepository>()
        val existingShop = getShop()
        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val scenario = getFragmentScenario(bundle, null, preferences)

        onView(
            allOf(
                isDescendantOfA(withId(R.id.tab_shop_options)),
                withText(R.string.tab_notes)
            )
        ).perform(ViewActions.click())

        val noteText = "Note added to shop"
        onView(withId(R.id.edt_notes))
            .perform(clearText())
            .perform(typeText(noteText))

        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val note = get<NoteRepository>().getAll().firstOrNull { it.noteText == noteText }
        assertNotNull(note)

        val updatedShop = locationRepository.get(existingShop.id)
        assertEquals(note.id, updatedShop?.noteId)
    }

    private suspend fun onCreateView_DefaultAisleTests_AssertAct(showDefaultAisle: Boolean): FragmentScenario<ShopFragment> {
        val existingShop = getShop()
        val locationRepository = get<LocationRepository>()
        locationRepository.update(existingShop.copy(showDefaultAisle = showDefaultAisle))
        val bundle = bundler.makeEditLocationBundle(existingShop.id)

        return getFragmentScenario(bundle)
    }

    @Test
    fun onCreateView_ShowDefaultAisleIsTrue_ShowNoAisleToggledOn() = runTest {
        onCreateView_DefaultAisleTests_AssertAct(true)

        onView(withId(R.id.swc_shop_show_unmapped_products))
            .check(matches(ViewMatchers.isChecked()))
    }

    @Test
    fun onCreateView_ShowDefaultAisleIsFalse_ShowNoAisleToggledOff() = runTest {
        onCreateView_DefaultAisleTests_AssertAct(false)

        onView(withId(R.id.swc_shop_show_unmapped_products))
            .check(matches(ViewMatchers.isNotChecked()))
    }

    @Test
    fun onSaveClick_ShowNoAisleToggleStatusSetToFalse_ShowDefaultAisleFalse() = runTest {
        val scenario = onCreateView_DefaultAisleTests_AssertAct(true)

        onView(withId(R.id.swc_shop_show_unmapped_products)).perform(ViewActions.click())
        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val updatedShop = getShop()
        assertEquals(false, updatedShop.showDefaultAisle)

        assertTrue(addEditFragmentListener.addEditSuccess)
        onView(withId(R.id.swc_shop_show_unmapped_products))
            .check(matches(ViewMatchers.isNotChecked()))
    }
}