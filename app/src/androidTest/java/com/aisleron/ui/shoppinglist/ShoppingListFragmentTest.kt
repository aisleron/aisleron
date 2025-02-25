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

package com.aisleron.ui.shoppinglist

import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withResourceName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.AddEditProductBundle
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.settings.ShoppingListPreferencesTestImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import java.lang.Thread.sleep
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ShoppingListFragmentTest : KoinTest {

    private lateinit var bundler: Bundler
    private lateinit var applicationTitleUpdateListener: ApplicationTitleUpdateListenerTestImpl
    private lateinit var fabHandler: FabHandlerTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    private fun getFragmentScenario(
        bundle: Bundle, shoppingListPreferencesTestImpl: ShoppingListPreferencesTestImpl? = null
    ): FragmentScenario<ShoppingListFragment> =
        launchFragmentInContainer<ShoppingListFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                ShoppingListFragment(
                    applicationTitleUpdateListener,
                    fabHandler,
                    shoppingListPreferencesTestImpl ?: ShoppingListPreferencesTestImpl()
                )
            }
        )

    private fun getLocation(locationType: LocationType): Location = runBlocking {
        get<LocationRepository>().getAll().first { it.type == locationType }
    }

    @Before
    fun setUp() {
        bundler = Bundler()
        applicationTitleUpdateListener = ApplicationTitleUpdateListenerTestImpl()
        fabHandler = FabHandlerTestImpl()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment =
            ShoppingListFragment.newInstance(
                1,
                FilterType.ALL,
                applicationTitleUpdateListener,
                fabHandler,
                ShoppingListPreferencesTestImpl()
            )
        Assert.assertNotNull(fragment)
    }

    @Test
    fun onCreateShoppingListFragment_HomeFilterIsInStock_AppTitleIsInStock() {
        val location = getLocation(LocationType.HOME)
        val bundle = bundler.makeShoppingListBundle(location.id, FilterType.IN_STOCK)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            assertEquals(
                it.getString(R.string.menu_in_stock),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShoppingListFragment_HomeFilterIsNeeded_AppTitleIsNeeded() {
        val location = getLocation(LocationType.HOME)
        val bundle = bundler.makeShoppingListBundle(location.id, FilterType.NEEDED)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            assertEquals(
                it.getString(R.string.menu_needed),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShoppingListFragment_HomeFilterIsAll_AppTitleIsShoppingList() {
        val location = getLocation(LocationType.HOME)
        val bundle = bundler.makeShoppingListBundle(location.id, FilterType.ALL)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            assertEquals(
                it.getString(R.string.menu_all_items),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShoppingListFragment_LocationTypeIsShop_AppTitleIsShopName() {
        val location = getLocation(LocationType.SHOP)
        val bundle = bundler.makeShoppingListBundle(location.id, location.defaultFilter)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            assertEquals(
                location.name,
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShoppingListFragment_BundleIsAttributes_FragmentCreated() {
        val location = getLocation(LocationType.HOME)
        val bundle = Bundle()
        bundle.putInt("locationId", location.id)
        bundle.putSerializable("filterType", FilterType.NEEDED)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            assertEquals(
                it.getString(R.string.menu_needed),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    private suspend fun getShoppingList(): Location {
        val locationRepository = get<LocationRepository>()
        val locationId = locationRepository.getAll().first { it.type != LocationType.HOME }.id
        return locationRepository.getLocationWithAislesWithProducts(locationId).first()!!
    }

    @Test
    fun onLongClick_IsAisleAndActionModeNotActive_ShowActionModeContextMenu() = runTest {
        val shoppingList = getShoppingList()
        getFragmentScenario(
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)
        )

        val aisleName = shoppingList.aisles.first().name
        //onView(withId(R.id.frg_shopping_list)).perform(RecyclerViewActions.actionOnHolderItem(withAisleName(aisleName), longClick()))

        onView(withText(aisleName)).perform(longClick())

        val actionBar = onView(withResourceName("action_mode_bar"))
        actionBar.check(matches(isDisplayed()))
        actionBar.check(matches(hasDescendant(withText(aisleName))))
        actionBar.check(matches(hasDescendant(withId(R.id.mnu_edit_shopping_list_item))))

        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).check(matches(isDisplayed()))
    }

    @Test
    fun onLongClick_IsProductAndActionModeNotActive_ShowActionModeContextMenu() = runTest {
        val shoppingList = getShoppingList()
        getFragmentScenario(
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)
        )

        val product = getProduct(shoppingList, false)

        onView(withText(product.name)).perform(longClick())

        val actionBar = onView(withResourceName("action_mode_bar"))
        actionBar.check(matches(isDisplayed()))
        actionBar.check(matches(hasDescendant(withText(product.name))))
        actionBar.check(matches(hasDescendant(withId(R.id.mnu_edit_shopping_list_item))))

        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).check(matches(isDisplayed()))
    }

    @Test
    fun onClick_ActionModeIsActive_DismissActionModeContextMenu() = runTest {
        val shoppingList = getShoppingList()
        val bundle = bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)
        val scenario = getFragmentScenario(bundle)
        val product = getProduct(shoppingList, false)

        val productItem = onView(allOf(withText(product.name), withId(R.id.txt_product_name)))
        productItem.perform(longClick())
        productItem.perform(click())
        sleep(500)

        val actionBar = onView(withResourceName("action_mode_bar"))
        actionBar.check(matches(not(isDisplayed())))
        scenario.onFragment {
            assertEquals(shoppingList.name, applicationTitleUpdateListener.appTitle)
        }
    }

    @Test
    fun onBackPress_ActionModeIsActive_DismissActionModeContextMenu() = runTest {
        val shoppingList = getShoppingList()
        val bundle = bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)
        val scenario = getFragmentScenario(bundle)
        val product = getProduct(shoppingList, false)

        val productItem = onView(allOf(withText(product.name), withId(R.id.txt_product_name)))
        productItem.perform(longClick())
        pressBack()
        sleep(500)

        val actionBar = onView(withResourceName("action_mode_bar"))
        actionBar.check(matches(not(isDisplayed())))
        scenario.onFragment {
            assertEquals(shoppingList.name, applicationTitleUpdateListener.appTitle)
        }
    }

    @Test
    fun onActionItemClicked_ActionItemIsDelete_DeleteDialogShown() = runTest {
        val shoppingList = getShoppingList()
        val product = getProduct(shoppingList, false)
        val bundle = bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)
        val scenario = getFragmentScenario(bundle)
        var deleteConfirmMessage = ""

        scenario.onFragment { fragment ->
            deleteConfirmMessage = fragment.getString(R.string.delete_confirmation, product.name)
        }

        val productItem = onView(allOf(withText(product.name), withId(R.id.txt_product_name)))
        productItem.perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())

        onView(withText(deleteConfirmMessage))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun onActionItemClicked_DeleteConfirmedOnProduct_ProductDeleted() = runTest {
        val shoppingList = getShoppingList()
        val product = getProduct(shoppingList, false)
        val bundle = bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(bundle)

        val productItem = onView(allOf(withText(product.name), withId(R.id.txt_product_name)))
        productItem.perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        val deletedProduct = get<ProductRepository>().getByName(product.name)
        Assert.assertNull(deletedProduct)
    }

    @Test
    fun onActionItemClicked_DeleteConfirmedOnAisle_AisleDeleted() = runTest {
        val shoppingList = getShoppingList()
        val aisle = shoppingList.aisles.first { !it.isDefault }
        val bundle = bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(bundle)
        val aisleItem = onView(allOf(withText(aisle.name), withId(R.id.txt_aisle_name)))
        aisleItem.perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        //TODO: Check item is deleted in recyclerview rather than db. Need to consider Flows
        val deletedAisle = get<AisleRepository>().get(aisle.id)
        Assert.assertNull(deletedAisle)
    }

    @Test
    fun onActionItemClicked_DeleteConfirmedOnDefaultAisle_ErrorSnackBarShown() = runTest {
        val shoppingList = getShoppingList()
        val aisle = shoppingList.aisles.first { it.isDefault }
        val bundle = bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(bundle)

        val aisleItem = onView(allOf(withText(aisle.name), withId(R.id.txt_aisle_name)))
        aisleItem.perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        )
    }

    @Test
    fun onActionItemClicked_DeleteCancelled_AisleNotDeleted() = runTest {
        val shoppingList = getShoppingList()
        val aisle = shoppingList.aisles.first { !it.isDefault }
        val bundle = bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(bundle)
        val aisleItem = onView(allOf(withText(aisle.name), withId(R.id.txt_aisle_name)))
        aisleItem.perform(longClick())
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.delete)).perform(click())
        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        val deletedAisle = get<AisleRepository>().get(aisle.id)
        Assert.assertNotNull(deletedAisle)
    }

    @Test
    fun onActionItemClicked_ActionItemIsEditOnProduct_NavigateToEditProduct() = runTest {
        val shoppingList = getShoppingList()
        val product = getProduct(shoppingList, false)
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario(shoppingListBundle).onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_shopping_list)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        val productItem = onView(allOf(withText(product.name), withId(R.id.txt_product_name)))
        productItem.perform(longClick())
        onView(withId(R.id.mnu_edit_shopping_list_item)).perform(click())

        val bundle = navController.backStack.last().arguments
        val addEditProductBundle = bundler.getAddEditProductBundle(bundle)

        assertEquals(product.id, addEditProductBundle.productId)
        assertEquals(AddEditProductBundle.ProductAction.EDIT, addEditProductBundle.actionType)
        assertEquals(R.id.nav_add_product, navController.currentDestination?.id)
    }

    @Test
    fun onProductStatusChange_SetProductInStock_ProductStatusToggled() = runTest {
        val shoppingList = getShoppingList()
        val product = getProduct(shoppingList, false)
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, FilterType.NEEDED)

        getFragmentScenario(shoppingListBundle)

        onView(
            allOf(
                withId(R.id.chk_in_stock),
                hasSibling(allOf(withText(product.name), withId(R.id.txt_product_name)))
            )
        ).perform(click())

        val updatedProduct = get<ProductRepository>().get(product.id)
        assertEquals(!product.inStock, updatedProduct?.inStock)
    }

    @Test
    fun onProductStatusChange_SetProductNeeded_ProductStatusToggled() = runTest {
        val shoppingList = getShoppingList()
        val product = getProduct(shoppingList, true)
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, FilterType.IN_STOCK)

        getFragmentScenario(shoppingListBundle)

        onView(
            allOf(
                withId(R.id.chk_in_stock),
                hasSibling(allOf(withText(product.name), withId(R.id.txt_product_name)))
            )
        ).perform(click())

        val updatedProduct = get<ProductRepository>().get(product.id)
        assertEquals(!product.inStock, updatedProduct?.inStock)
    }

    @Test
    fun onSwipe_IsProduct_ProductStatusToggled() = runTest {
        val shoppingList = getShoppingList()
        val product =
            getProduct(shoppingList, false)
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, FilterType.NEEDED)

        getFragmentScenario(shoppingListBundle)

        onView(
            allOf(
                withText(product.name),
                withId(R.id.txt_product_name)
            )
        ).perform(ViewActions.swipeLeft())

        val updatedProduct = get<ProductRepository>().get(product.id)
        assertEquals(!product.inStock, updatedProduct?.inStock)
    }

    private fun getProduct(shoppingList: Location, inStock: Boolean): Product {
        val product =
            shoppingList.aisles.first {
                it.products.count { ap -> ap.product.inStock == inStock } > 0 && !it.isDefault
            }.products.first { it.product.inStock == inStock }.product
        return product
    }

    @Test
    fun onActionItemClicked_ActionItemIsEditOnAisle_ShowAisleEditDialog() = runTest {
        val shoppingList = getShoppingList()
        val aisle = shoppingList.aisles.first()
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(shoppingListBundle)

        val aisleItem = onView(allOf(withText(aisle.name), withId(R.id.txt_aisle_name)))
        aisleItem.perform(longClick())
        onView(withId(R.id.mnu_edit_shopping_list_item)).perform(click())

        onView(withText(R.string.edit_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(withText(aisle.name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun aisleEditDialog_DoneClicked_AisleUpdated() = runTest {
        val shoppingList = getShoppingList()
        val aisle = shoppingList.aisles.first()
        val updateSuffix = " Updated"
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(shoppingListBundle)

        val aisleItem = onView(allOf(withText(aisle.name), withId(R.id.txt_aisle_name)))
        aisleItem.perform(longClick())
        onView(withId(R.id.mnu_edit_shopping_list_item)).perform(click())

        onView(allOf(withText(aisle.name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(updateSuffix))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val updatedAisle = get<AisleRepository>().get(aisle.id)

        assertEquals(aisle.name + updateSuffix, updatedAisle?.name)
    }

    @Test
    fun aisleEditDialog_CancelClicked_AisleNotUpdated() = runTest {
        val shoppingList = getShoppingList()
        val aisle = shoppingList.aisles.first()
        val updateSuffix = " Updated"
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(shoppingListBundle)

        val aisleItem = onView(allOf(withText(aisle.name), withId(R.id.txt_aisle_name)))
        aisleItem.perform(longClick())
        onView(withId(R.id.mnu_edit_shopping_list_item)).perform(click())

        onView(allOf(withText(aisle.name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(updateSuffix))

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        val updatedAisle = get<AisleRepository>().get(aisle.id)

        assertEquals(aisle.name, updatedAisle?.name)
    }

    @Test
    fun aisleEditDialog_DoneClickedWithBlankAisleName_AisleNotUpdated() = runTest {
        val shoppingList = getShoppingList()
        val aisle = shoppingList.aisles.first()
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(shoppingListBundle)

        val aisleItem = onView(allOf(withText(aisle.name), withId(R.id.txt_aisle_name)))
        aisleItem.perform(longClick())
        onView(withId(R.id.mnu_edit_shopping_list_item)).perform(click())

        onView(allOf(withText(aisle.name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(clearText())

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val updatedAisle = get<AisleRepository>().get(aisle.id)

        assertEquals(aisle.name, updatedAisle?.name)
    }

    @Test
    fun onClickFab_IsAddProductFab_NavigateToAddProduct() = runTest {
        val shoppingList = getShoppingList()
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario(shoppingListBundle).onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            navController.setCurrentDestination(R.id.nav_shopping_list)
            Navigation.setViewNavController(fragment.requireView(), navController)
            fabHandler.clickFab(FabHandler.FabOption.ADD_PRODUCT, fragment.requireView())
        }

        val bundle = navController.backStack.last().arguments
        val addEditProductBundle = bundler.getAddEditProductBundle(bundle)

        assertNull(addEditProductBundle.name)
        assertEquals(AddEditProductBundle.ProductAction.ADD, addEditProductBundle.actionType)
        assertEquals(
            shoppingList.defaultFilter == FilterType.IN_STOCK,
            addEditProductBundle.inStock
        )
        assertEquals(R.id.nav_add_product, navController.currentDestination?.id)
    }

    @Test
    fun onClickFab_IsAddAisleFab_ShowAddAisleDialog() = runTest {
        val shoppingList = getShoppingList()
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(shoppingListBundle).onFragment {
            fabHandler.clickFab(FabHandler.FabOption.ADD_AISLE, it.requireView())
        }

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(withText("")))
    }

    @Test
    fun aisleDialogDoneClick_HasAisleName_AddNewAisle() = runTest {
        val newAisleName = "Add Aisle Test 123321"
        val shoppingList = getShoppingList()
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(shoppingListBundle).onFragment {
            fabHandler.clickFab(FabHandler.FabOption.ADD_AISLE, it.requireView())
        }

        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }

        assertNotNull(addedAisle)
    }

    @Test
    fun aisleDialogDoneClick_AisleNameIsEmpty_NoAisleAdded() = runTest {
        val shoppingList = getShoppingList()
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()

        getFragmentScenario(shoppingListBundle).onFragment {
            fabHandler.clickFab(FabHandler.FabOption.ADD_AISLE, it.requireView())
        }

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val aisleCountAfter = aisleRepository.getAll().count()

        assertEquals(aisleCountBefore, aisleCountAfter)
    }

    @Test
    fun aisleDialogCancelClick_RegardlessOfAisleName_NoAisleAdded() = runTest {
        val newAisleName = "Add Aisle Test 123321"
        val shoppingList = getShoppingList()
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()

        getFragmentScenario(shoppingListBundle).onFragment {
            fabHandler.clickFab(FabHandler.FabOption.ADD_AISLE, it.requireView())
        }

        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        val aisleCountAfter = aisleRepository.getAll().count()
        val addedAisle = aisleRepository.getAll().firstOrNull { it.name == newAisleName }

        assertEquals(aisleCountBefore, aisleCountAfter)
        assertNull(addedAisle)
    }

    @Test
    fun aisleDialogAddAnotherClick_HasAisleName_AisleAddedAndShowDialogAgain() = runTest {
        val newAisleName = "Add Aisle Test 123321"
        val shoppingList = getShoppingList()
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, shoppingList.defaultFilter)

        getFragmentScenario(shoppingListBundle).onFragment {
            fabHandler.clickFab(FabHandler.FabOption.ADD_AISLE, it.requireView())
        }

        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(R.string.add_another))
            .inRoot(isDialog())
            .perform(click())

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        assertNotNull(addedAisle)
    }

    @Test
    fun onProductStatusChange_StatusUpdateSnackBarEnabled_ShowSnackBar() = runTest {
        val shoppingList = getShoppingList()
        val product = getProduct(shoppingList, false)
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, FilterType.NEEDED)

        val shoppingListPreferencesTestImpl = ShoppingListPreferencesTestImpl()
        shoppingListPreferencesTestImpl.setHideStatusChangeSnackBar(false)

        getFragmentScenario(shoppingListBundle, shoppingListPreferencesTestImpl)

        onView(
            allOf(
                withId(R.id.chk_in_stock),
                hasSibling(allOf(withText(product.name), withId(R.id.txt_product_name)))
            )
        ).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        )
    }

    @Test
    fun onProductStatusChange_StatusUpdateSnackBarDisabled_HideSnackBar() = runTest {
        val shoppingList = getShoppingList()
        val product = getProduct(shoppingList, false)
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, FilterType.NEEDED)

        val shoppingListPreferencesTestImpl = ShoppingListPreferencesTestImpl()
        shoppingListPreferencesTestImpl.setHideStatusChangeSnackBar(true)

        getFragmentScenario(shoppingListBundle, shoppingListPreferencesTestImpl)

        onView(
            allOf(
                withId(R.id.chk_in_stock),
                hasSibling(allOf(withText(product.name), withId(R.id.txt_product_name)))
            )
        ).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(doesNotExist())
    }

    @Test
    fun onProductStatusChange_StatusUpdateSnackBarUndoClicked_ProductStatusChanged() = runTest {
        val shoppingList = getShoppingList()
        val product = getProduct(shoppingList, false)
        val shoppingListBundle =
            bundler.makeShoppingListBundle(shoppingList.id, FilterType.NEEDED)

        val productStatusBefore = product.inStock

        val shoppingListPreferencesTestImpl = ShoppingListPreferencesTestImpl()
        shoppingListPreferencesTestImpl.setHideStatusChangeSnackBar(false)

        getFragmentScenario(shoppingListBundle, shoppingListPreferencesTestImpl)

        onView(
            allOf(
                withId(R.id.chk_in_stock),
                hasSibling(allOf(withText(product.name), withId(R.id.txt_product_name)))
            )
        ).perform(click())

        val productRepository = get<ProductRepository>()
        val productStatusAfterChange = productRepository.get(product.id)?.inStock

        onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click())

        val productStatusAfterUndo = productRepository.get(product.id)?.inStock

        assertNotEquals(productStatusBefore, productStatusAfterChange)
        assertEquals(productStatusBefore, productStatusAfterUndo)
    }

    /*@Test
    fun onDrag_IsProduct_ProductRankUpdated() {
        val shoppingList = getShoppingList()
        val product =
            shoppingList.aisles.first { it.products.isNotEmpty() }.products.first { !it.product.inStock }.product
        val shoppingListBundle = bundler.makeShoppingListBundle(shoppingList.id, FilterType.NEEDED)

        getFragmentScenario(shoppingListBundle)

        onView(
            allOf(
                withText(product.name),
                withId(R.id.txt_product_name)
            )
        ).perform(ViewActions.swipeUp())

        val updatedProduct = runBlocking { testData.productRepository.get(product.id) }
        assertEquals(!product.inStock, updatedProduct?.inStock)
    }*/

    /*
        Reorder
            Aisle
            Product
     */
}