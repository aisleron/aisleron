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
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.location.usecase.AddLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module

class ShopFragmentTest {
    private lateinit var bundler: Bundler
    private lateinit var addEditFragmentListener: TestAddEditFragmentListener
    private lateinit var testData: TestDataManager
    private lateinit var fabHandler: FabHandlerTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getKoinModules(): List<Module> {
        testData = TestDataManager()
        val shopViewModel = ShopViewModel(
            addLocationUseCase = AddLocationUseCaseImpl(
                testData.locationRepository,
                AddAisleUseCaseImpl(
                    testData.aisleRepository,
                    GetLocationUseCase(testData.locationRepository)
                ),
                GetAllProductsUseCase(testData.productRepository),
                AddAisleProductsUseCase(testData.aisleProductRepository),
                IsLocationNameUniqueUseCase(testData.locationRepository)
            ),
            updateLocationUseCase = UpdateLocationUseCase(
                testData.locationRepository,
                IsLocationNameUniqueUseCase(testData.locationRepository)
            ),
            getLocationUseCase = GetLocationUseCase(testData.locationRepository),
            TestScope(UnconfinedTestDispatcher())
        )

        return listOf(
            module {
                factory<ShopViewModel> { shopViewModel }
            }
        )
    }

    @Before
    fun setUp() {
        bundler = Bundler()
        addEditFragmentListener = TestAddEditFragmentListener()
        fabHandler = FabHandlerTestImpl()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun onCreateShopFragment_HasEditBundle_AppTitleIsEdit() {
        val bundle = bundler.makeEditLocationBundle(1)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.edit_location),
                addEditFragmentListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShopFragment_HasEditBundle_ScreenMatchesEditLocation() {
        val existingShop = runBlocking {
            testData.locationRepository.getAll().first { it.pinned }
        }

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
                addEditFragmentListener.appTitle
            )
        }
    }

    @Test
    fun onSaveClick_NewShopHasUniqueName_ShopSaved() {
        val bundle = bundler.makeAddLocationBundle("New Location")
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()
        val newShopName = "Shop Add New Test"

        onView(withId(R.id.edt_shop_name)).perform(typeText(newShopName))
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val shop = runBlocking {
            testData.locationRepository.getAll().firstOrNull { it.name == newShopName }
        }

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
    fun onSaveClick_ExistingShopHasUniqueName_ShopUpdated() {
        val existingShop = runBlocking {
            testData.locationRepository.getAll().first()
        }

        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()
        val newShopName = existingShop.name + " Updated"

        onView(withId(R.id.edt_shop_name))
            .perform(ViewActions.clearText())
            .perform(typeText(newShopName))
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val updatedShop = runBlocking { testData.locationRepository.get(existingShop.id) }

        onView(withId(R.id.edt_shop_name)).check(matches(ViewMatchers.withText(newShopName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(updatedShop)
        Assert.assertEquals(newShopName, updatedShop?.name)
    }

    @Test
    fun onSaveClick_PinnedStatusChanged_PinnedStatusUpdated() {
        val existingShop = runBlocking {
            testData.locationRepository.getAll().first { !it.pinned }
        }

        val bundle = bundler.makeEditLocationBundle(existingShop.id)
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()

        onView(withId(R.id.swc_shop_pinned)).perform(ViewActions.click())
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val updatedShop = runBlocking { testData.locationRepository.get(existingShop.id) }

        onView(withId(R.id.swc_shop_pinned)).check(matches(ViewMatchers.isChecked()))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertEquals(existingShop.copy(pinned = !existingShop.pinned), updatedShop)
    }

    @Test
    fun onSaveClick_IsDuplicateName_ShowErrorSnackBar() {
        val existingShop = runBlocking {
            testData.locationRepository.getAll().first()
        }

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
        val fragment = ShopFragment.newInstance(null, addEditFragmentListener)
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
            instantiate = { ShopFragment(addEditFragmentListener, fabHandler) }
        )

        return scenario
    }

    class TestAddEditFragmentListener : AddEditFragmentListener {
        var appTitle: String = ""
        var addEditSuccess: Boolean = false
        override fun applicationTitleUpdated(newTitle: String) {
            appTitle = newTitle
        }

        override fun addEditActionCompleted() {
            addEditSuccess = true
        }
    }
}