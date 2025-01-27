package com.aisleron.ui.welcome

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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.MainActivity
import com.aisleron.R
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.di.TestAppModules
import com.aisleron.domain.product.Product
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.settings.WelcomePreferencesTestImpl
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WelcomeFragmentTest {

    private lateinit var addEditFragmentListener: AddEditFragmentListenerTestImpl
    private lateinit var testData: TestDataManager
    private lateinit var fabHandler: FabHandlerTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> {
        testData = TestDataManager(false)
        return TestAppModules().getTestAppModules(testData)
    }

    private fun getFragmentScenario(
        welcomePreferences: WelcomePreferencesTestImpl? = null
    ): FragmentScenario<WelcomeFragment> =
        launchFragmentInContainer<WelcomeFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                WelcomeFragment(
                    fabHandler,
                    welcomePreferences ?: WelcomePreferencesTestImpl(),
                    addEditFragmentListener
                )
            }
        )

    @Before
    fun setUp() {
        addEditFragmentListener = AddEditFragmentListenerTestImpl()
        fabHandler = FabHandlerTestImpl()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment =
            WelcomeFragment.newInstance(
                fabHandler,
                WelcomePreferencesTestImpl(),
                addEditFragmentListener
            )
        Assert.assertNotNull(fragment)
    }

    @Test
    fun applicationStarted_AppNotInitialized_WelcomeScreenDisplayed() {
        SharedPreferencesInitializer().invoke(isInitialized = false)
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
        SharedPreferencesInitializer().invoke(isInitialized = true)
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
    fun welcomePage_SelectAddOwnProducts_NoDataAdded() {
        val productCountBefore = runBlocking { testData.productRepository.getAll().count() }
        val locationCountBefore = runBlocking { testData.locationRepository.getAll().count() }
        val aisleCountBefore = runBlocking { testData.aisleRepository.getAll().count() }

        getFragmentScenario()

        val welcomeOption = onView(withId(R.id.txt_welcome_add_own_product))
        welcomeOption.perform(click())

        val productCountAfter = runBlocking { testData.productRepository.getAll().count() }
        val locationCountAfter = runBlocking { testData.locationRepository.getAll().count() }
        val aisleCountAfter = runBlocking { testData.aisleRepository.getAll().count() }

        assertEquals(productCountBefore, productCountAfter)
        assertEquals(locationCountBefore, locationCountAfter)
        assertEquals(aisleCountBefore, aisleCountAfter)
    }

    @Test
    fun welcomePage_SelectAddOwnProducts_AddEditListenerCalled() {
        val addEditSuccessBefore = addEditFragmentListener.addEditSuccess

        getFragmentScenario()

        val welcomeOption = onView(withId(R.id.txt_welcome_add_own_product))
        welcomeOption.perform(click())

        Assert.assertFalse(addEditSuccessBefore)
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
    }

    @Test
    fun welcomePage_SelectAddOwnProducts_InitializeOptionSet() {
        val welcomePreferences = WelcomePreferencesTestImpl()
        val initialisedBefore = welcomePreferences.isInitialized(getInstrumentation().targetContext)

        getFragmentScenario(welcomePreferences)

        val welcomeOption = onView(withId(R.id.txt_welcome_add_own_product))
        welcomeOption.perform(click())

        Assert.assertFalse(initialisedBefore)
        Assert.assertTrue(welcomePreferences.isInitialized(getInstrumentation().targetContext))
    }

    @Test
    fun welcomePage_SelectLoadSampleItems_DataAdded() {
        val productCountBefore = runBlocking { testData.productRepository.getAll().count() }
        val locationCountBefore = runBlocking { testData.locationRepository.getAll().count() }
        val aisleCountBefore = runBlocking { testData.aisleRepository.getAll().count() }

        getFragmentScenario()

        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.perform(click())

        val productCountAfter = runBlocking { testData.productRepository.getAll().count() }
        val locationCountAfter = runBlocking { testData.locationRepository.getAll().count() }
        val aisleCountAfter = runBlocking { testData.aisleRepository.getAll().count() }

        assertTrue(productCountBefore < productCountAfter)
        assertTrue(locationCountBefore < locationCountAfter)
        assertTrue(aisleCountBefore < aisleCountAfter)
    }

    @Test
    fun welcomePage_SelectLoadSampleItems_AddEditListenerCalled() {
        val addEditSuccessBefore = addEditFragmentListener.addEditSuccess

        getFragmentScenario()

        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.perform(click())

        Assert.assertFalse(addEditSuccessBefore)
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
    }

    @Test
    fun welcomePage_SelectLoadSampleItems_InitializeOptionSet() {
        val welcomePreferences = WelcomePreferencesTestImpl()
        val initialisedBefore = welcomePreferences.isInitialized(getInstrumentation().targetContext)

        getFragmentScenario(welcomePreferences)

        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.perform(click())

        Assert.assertFalse(initialisedBefore)
        Assert.assertTrue(welcomePreferences.isInitialized(getInstrumentation().targetContext))
    }

    @Test
    fun selectLoadSampleItems_HasExistingProducts_ShowErrorSnackBar() {
        val welcomePreferences = WelcomePreferencesTestImpl()
        runBlocking {
            testData.productRepository.add(
                Product(
                    id = 0,
                    name = "Welcome Page Sample Items Error Test",
                    inStock = false
                )
            )
        }

        getFragmentScenario(welcomePreferences)

        val welcomeOption = onView(withId(R.id.txt_welcome_load_sample_items))
        welcomeOption.perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        )

        Assert.assertFalse(welcomePreferences.isInitialized(getInstrumentation().targetContext))
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
    fun selectedDbImport_BackPressedOnSettings_ReturnToMainScreen() {
        SharedPreferencesInitializer().invoke(isInitialized = false)
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        val welcomeOption = onView(withId(R.id.txt_welcome_import_db))
        welcomeOption.perform(click())

        scenario.use { s ->
            s.onActivity { a ->
                val navController = a.findNavController(R.id.nav_host_fragment_content_main)
                navController.popBackStack()

                assertEquals(R.id.nav_in_stock, navController.currentDestination?.id)
            }
        }
    }

    @Test
    fun welcomePage_BackPressed_InitializeOptionNotSet() {
        SharedPreferencesInitializer().invoke(isInitialized = false)
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
}