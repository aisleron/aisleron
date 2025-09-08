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

package com.aisleron.ui.welcome

import android.app.Instrumentation
import android.content.Intent
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.MainActivity
import com.aisleron.R
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.inMemoryDatabaseTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.settings.WelcomePreferencesTestImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WelcomeFragmentTest : KoinTest {
    private lateinit var fabHandler: FabHandlerTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    private fun getFragmentScenario(
        welcomePreferences: WelcomePreferencesTestImpl? = null
    ): FragmentScenario<WelcomeFragment> =
        launchFragmentInContainer<WelcomeFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                WelcomeFragment(
                    fabHandler,
                    welcomePreferences ?: WelcomePreferencesTestImpl()
                )
            }
        )

    @Before
    fun setUp() {
        fabHandler = FabHandlerTestImpl()
    }

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment =
            WelcomeFragment.newInstance(
                fabHandler,
                WelcomePreferencesTestImpl()
            )
        Assert.assertNotNull(fragment)
    }

    @Test
    fun applicationStarted_AppNotInitialized_WelcomeScreenDisplayed() {
        loadKoinModules(listOf(preferenceTestModule, fragmentModule, generalTestModule))
        SharedPreferencesInitializer().setIsInitialized(false)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.use { s ->
            s.onActivity { a ->
                val navController = a.findNavController(R.id.nav_host_fragment_content_main)

                assertEquals(R.id.nav_welcome, navController.currentDestination?.id)
                assertEquals(a.getString(R.string.welcome_app_title), a.supportActionBar?.title)
            }
        }
    }

    @Test
    fun applicationStarted_AppInitialized_WelcomeScreenNotDisplayed() {
        loadKoinModules(
            listOf(
                preferenceTestModule,
                fragmentModule,
                generalTestModule,
                inMemoryDatabaseTestModule
            )
        )
        SharedPreferencesInitializer().setIsInitialized(true)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.use { s ->
            s.onActivity { a ->
                val navController = a.findNavController(R.id.nav_host_fragment_content_main)

                assertEquals(R.id.nav_in_stock, navController.currentDestination?.id)
                assertEquals(a.getString(R.string.app_name), a.supportActionBar?.title)
            }
        }
    }

    @Test
    fun welcomePage_SelectAddOwnProducts_NoDataAdded() = runTest {
        val productCountBefore = get<ProductRepository>().getAll().count()
        val locationCountBefore = get<LocationRepository>().getAll().count()
        val aisleCountBefore = get<AisleRepository>().getAll().count()

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_welcome)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        val welcomeOption = onView(withId(R.id.txt_welcome_add_own_product))
        welcomeOption.perform(click())

        val productCountAfter = get<ProductRepository>().getAll().count()
        val locationCountAfter = get<LocationRepository>().getAll().count()
        val aisleCountAfter = get<AisleRepository>().getAll().count()

        assertEquals(productCountBefore, productCountAfter)
        assertEquals(locationCountBefore, locationCountAfter)
        assertEquals(aisleCountBefore, aisleCountAfter)
        Assert.assertEquals(R.id.nav_in_stock, navController.currentDestination?.id)
    }

    @Test
    fun welcomePage_SelectAddOwnProducts_InitializeOptionSet() {
        val welcomePreferences = WelcomePreferencesTestImpl()
        val initialisedBefore = welcomePreferences.isInitialized(getInstrumentation().targetContext)

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario(welcomePreferences).onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_welcome)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        val welcomeOption = onView(withId(R.id.txt_welcome_add_own_product))
        welcomeOption.perform(click())

        Assert.assertFalse(initialisedBefore)
        Assert.assertTrue(welcomePreferences.isInitialized(getInstrumentation().targetContext))
    }

    @Test
    fun welcomePage_SelectLoadSampleItems_DataAdded() = runTest {
        val productCountBefore = get<ProductRepository>().getAll().count()
        val locationCountBefore = get<LocationRepository>().getAll().count()
        val aisleCountBefore = get<AisleRepository>().getAll().count()

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_welcome)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.perform(click())

        val productCountAfter = get<ProductRepository>().getAll().count()
        val locationCountAfter = get<LocationRepository>().getAll().count()
        val aisleCountAfter = get<AisleRepository>().getAll().count()

        assertTrue(productCountBefore < productCountAfter)
        assertTrue(locationCountBefore < locationCountAfter)
        assertTrue(aisleCountBefore < aisleCountAfter)
        Assert.assertEquals(R.id.nav_in_stock, navController.currentDestination?.id)
    }

    @Test
    fun welcomePage_SelectLoadSampleItems_InitializeOptionSet() {
        val welcomePreferences = WelcomePreferencesTestImpl()
        val initialisedBefore = welcomePreferences.isInitialized(getInstrumentation().targetContext)

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario(welcomePreferences).onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_welcome)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.perform(click())

        Assert.assertFalse(initialisedBefore)
        Assert.assertTrue(welcomePreferences.isInitialized(getInstrumentation().targetContext))
    }

    @Test
    fun selectLoadSampleItems_HasExistingProducts_LoadSampleItemsDisabled() {
        runBlocking {
            get<ProductRepository>().add(
                Product(
                    id = 0,
                    name = "Welcome Page Sample Items Error Test",
                    inStock = false,
                    qtyNeeded = 0
                )
            )
        }

        getFragmentScenario()

        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.check(matches(not(isEnabled())))
    }

    @Test
    fun selectLoadSampleItems_NoExistingProducts_LoadSampleItemsEnabled() {
        getFragmentScenario()
        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.check(matches(isEnabled()))
    }

    @Test
    fun welcomePage_SelectRestoreDatabase_InitializeOptionSet() {
        val welcomePreferences = WelcomePreferencesTestImpl()
        val initialisedBefore = welcomePreferences.isInitialized(getInstrumentation().targetContext)

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario(welcomePreferences).onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_welcome)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        val welcomeOption = onView(withId(R.id.txt_welcome_import_db))
        welcomeOption.perform(click())

        Assert.assertFalse(initialisedBefore)
        Assert.assertTrue(welcomePreferences.isInitialized(getInstrumentation().targetContext))
    }

    @Test
    fun welcomePage_SelectRestoreDatabase_NavigateToSettings() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_welcome)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        val welcomeOption = onView(withId(R.id.txt_welcome_import_db))
        welcomeOption.perform(click())

        Assert.assertEquals(R.id.nav_settings, navController.currentDestination?.id)
    }

    @Test
    fun welcomePage_BackPressed_InitializeOptionNotSet() {
        loadKoinModules(
            listOf(
                preferenceTestModule,
                fragmentModule,
                generalTestModule,
                inMemoryDatabaseTestModule
            )
        )
        SharedPreferencesInitializer().setIsInitialized(false)
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        val welcomeOption = onView(withId(R.id.txt_welcome_import_db))
        welcomeOption.perform(click())

        scenario.use { s ->
            s.onActivity { a ->
                val navController = a.findNavController(R.id.nav_host_fragment_content_main)
                navController.popBackStack()

                val isInitialised = PreferenceManager.getDefaultSharedPreferences(a)
                    .getBoolean("is_initialised", true)

                assertFalse(isInitialised)
            }
        }
    }

    @Test
    fun welcomePage_SelectViewDocumentation_OpensDocumentationUrl() {
        getFragmentScenario()
        Intents.init()

        var documentsUri = ""

        getFragmentScenario().onFragment { fragment ->
            documentsUri = fragment.getString(R.string.aisleron_documentation_url)
        }


        val expectedIntent = Matchers.allOf(hasAction(Intent.ACTION_VIEW), hasData(documentsUri))
        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        val welcomeOption = onView(withId(R.id.txt_welcome_documentation))
        welcomeOption.perform(click())
        intended(expectedIntent)

        Intents.release()
    }

    @Test
    fun onViewModelStateChange_IsError_ShowErrorSnackBar() = runTest {
        val exceptionMessage = "Error on load sample products"
        declare<CreateSampleDataUseCase> {
            object : CreateSampleDataUseCase {
                override suspend fun invoke() {
                    throw Exception(exceptionMessage)
                }
            }
        }

        getFragmentScenario()
        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                allOf(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                    withText(startsWith("ERROR:"))
                )
            )
        )
    }
}