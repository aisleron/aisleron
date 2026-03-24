/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.screenshots

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.ActionBarContextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.BuildConfig
import com.aisleron.MainActivity
import com.aisleron.R
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.factoryModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.generalModule
import com.aisleron.di.preferenceModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardToLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCase
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceTestImpl
import com.aisleron.ui.FabHandler
import com.aisleron.utils.SystemIds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import tools.fastlane.screengrab.Screengrab
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Suppress("SameParameterValue")
@RunWith(AndroidJUnit4::class)
class CaptureScreenshots : KoinTest {
    private val saveBigSuper = "Save Big Super"


    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule,
            fragmentModule,
            viewModelTestModule,
            repositoryModule,
            useCaseModule,
            generalModule,
            preferenceModule,
            factoryModule
        )
    )

    @Before
    fun setUp() {
        val prefs = SharedPreferencesInitializer()
        prefs.clearPreferences()
        prefs.setIsInitialized(true)
        prefs.setLastUpdateCode(BuildConfig.VERSION_CODE)
        prefs.setLastUpdateName(BuildConfig.VERSION_NAME)
        prefs.setPureBlackStyle(SharedPreferencesInitializer.PureBlackStyle.DEFAULT)
        val uiMode = InstrumentationRegistry.getArguments().getString("uiMode", "light")
        val appTheme = if (uiMode == "dark") {
            Log.d("Theme", "Dark")
            SharedPreferencesInitializer.ApplicationTheme.DARK_THEME
        } else {
            Log.d("Theme", "Light")
            SharedPreferencesInitializer.ApplicationTheme.LIGHT_THEME
        }

        prefs.setApplicationTheme(appTheme)

        runBlocking {
            get<CreateSampleDataUseCase>().invoke()
            createCornerConvenience()

            val frozenVeg = get<ProductRepository>().getByName("Frozen Vegetables")
            frozenVeg?.let {
                get<UpdateProductStatusUseCase>().invoke(it.id, false)
            }

            // Add a dummy loyalty card for screenshots
            val location = get<LocationRepository>().getByName("Save Big Supermarket")!!
            get<UpdateLocationUseCase>().invoke(location.copy(name = saveBigSuper))


            val loyaltyCardId = get<AddLoyaltyCardUseCase>().invoke(
                LoyaltyCard(
                    id = 0,
                    name = saveBigSuper,
                    provider = LoyaltyCardProviderType.CATIMA,
                    intent = "dummy-intent"
                )
            )

            get<AddLoyaltyCardToLocationUseCase>().invoke(location.id, loyaltyCardId)
        }
    }

    private suspend fun createCornerConvenience() {
        val locationId = get<AddLocationUseCase>().invoke(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = "Corner Convenience",
                pinned = false,
                aisles = emptyList(),
                showDefaultAisle = true,
                expanded = true,
                rank = 10
            )
        )

        val defaultAisleId = get<AisleRepository>().getAll()
            .first { it.isDefault && it.locationId == locationId }.id

        val aisleId = get<AisleRepository>().add(
            Aisle(
                name = "Spices",
                products = emptyList(),
                locationId = locationId,
                rank = 1,
                id = 0,
                isDefault = false,
                expanded = true
            )
        )

        val salt = get<ProductRepository>().getByName("Salt")
        salt?.let {
            get<UpdateProductStatusUseCase>().invoke(it.id, false)
            get<ChangeProductAisleUseCase>().invoke(it.id, defaultAisleId, aisleId)
        }
    }

    private fun takeScreenshot(screenshotName: String, waitMillis: Long = 0) {
        sleep(waitMillis)
        Screengrab.screenshot(screenshotName)
    }

    private fun getActivityScenario(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.app_bar_main))
            .check(matches(isDisplayed()))

        return scenario
    }

    private suspend fun deleteProducts() {
        val repo = get<ProductRepository>()
        repo.getAll().forEach {
            repo.remove(it)
        }
    }

    @Test
    fun screenshot_WelcomePage() = runTest {
        deleteProducts()
        SharedPreferencesInitializer().setIsInitialized(false)
        getActivityScenario().use {
            takeScreenshot("alr-010-welcome-page")
        }
    }

    @Test
    fun screenshot_BlankList() = runTest {
        deleteProducts()
        getActivityScenario().use {
            takeScreenshot("alr-020-blank-list")
        }
    }

    @Test
    fun screenshot_SampleItemsList() {
        getActivityScenario().use {
            takeScreenshot("alr-030-sample-items-list")
        }
    }

    private fun openNavigationDrawer() {
        onView(withId(R.id.drawer_layout))
            .perform(DrawerActions.open())
    }

    @Test
    fun screenshot_NavigationDrawer() {
        getActivityScenario().use {
            openNavigationDrawer()
            takeScreenshot("alr-040-navigation-drawer")
        }
    }

    private fun navigateToSettings() {
        declare<DatabaseMaintenance> {
            DatabaseMaintenanceTestImpl()
        }

        openNavigationDrawer()
        onView(withId(R.id.nav_settings))
            .perform(click())
    }

    @Test
    fun screenshot_Settings() {
        getActivityScenario().use {
            navigateToSettings()
            takeScreenshot("alr-050-settings")
        }
    }

    private fun clickFab(fabOption: FabHandler.FabOption) {
        clickMainFab()

        val fabId = when (fabOption) {
            FabHandler.FabOption.ADD_PRODUCT -> R.id.fab_add_product
            FabHandler.FabOption.ADD_AISLE -> R.id.fab_add_aisle
            FabHandler.FabOption.ADD_SHOP -> R.id.fab_add_shop
            FabHandler.FabOption.SEARCH -> R.id.fab_search
        }

        onView(withId(fabId))
            .perform(click())
    }

    @Test
    fun screenshot_AddProduct() {
        getActivityScenario().use {
            clickFab(FabHandler.FabOption.ADD_PRODUCT)
            takeScreenshot("alr-060-add-product")
        }
    }

    private fun clickProductExtraOptions() {
        onView(withId(R.id.txt_toggle_extra_options))
            .perform(click())
    }

    @Test
    fun screenshot_ProductExtraOptions() {
        getActivityScenario().use {
            clickFab(FabHandler.FabOption.ADD_PRODUCT)
            Espresso.closeSoftKeyboard()
            clickProductExtraOptions()
            takeScreenshot("alr-065-product-extra-options")
        }
    }

    private fun selectShoppingListItem(productName: String) {
        onView(withText(productName))
            .perform(longClick())
    }

    @Test
    fun screenshot_SelectProduct() {
        getActivityScenario().use {
            selectShoppingListItem("Apples")
            takeScreenshot("alr-070-select-product")
        }
    }

    private fun clickEditShoppingListItem() {
        onView(withId(R.id.mnu_edit_shopping_list_item))
            .perform(click())
    }

    @Test
    fun screenshot_EditProduct() {
        getActivityScenario().use {
            selectShoppingListItem("Apples")
            clickEditShoppingListItem()
            takeScreenshot("alr-080-edit-product")
        }
    }

    private fun selectProductExtrasTab(@StringRes tabName: Int) {
        onView(withText(tabName))
            .perform(click())
    }

    private fun selectProductAislesEntry(locationName: String) {
        onView(allOf(withId(R.id.location_name), withText(locationName)))
            .perform(click())

    }

    @Test
    fun screenshot_ProductSelectAisle() {
        getActivityScenario().use {
            selectShoppingListItem("Apples")
            clickEditShoppingListItem()
            Espresso.closeSoftKeyboard()
            clickProductExtraOptions()

            selectProductExtrasTab(R.string.product_tab_aisles)
            takeScreenshot("alr-085-010-product-aisles")

            selectProductAislesEntry(saveBigSuper)
            takeScreenshot("alr-085-020-product-aisle-picker")
        }
    }

    @Test
    fun screenshot_ProductInventory() = runTest {
        val productRepository = get<ProductRepository>()
        val product = productRepository.getByName("Apples")!!.copy(
            unitOfMeasure = "kg",
            trackingMode = TrackingMode.CHECKBOX_QUANTITY,
            qtyIncrement = 0.5
        )

        productRepository.update(product)

        getActivityScenario().use {
            selectShoppingListItem(product.name)
            clickEditShoppingListItem()
            Espresso.closeSoftKeyboard()
            clickProductExtraOptions()

            selectProductExtrasTab(R.string.product_tab_aisles)
            selectProductExtrasTab(R.string.product_tab_inventory)
            takeScreenshot("alr-086-010-product-inventory")
        }
    }


    private fun openCabOverflowMenu() {
        onView(
            allOf(
                withContentDescription("More options"),
                isDescendantOfA(isAssignableFrom(ActionBarContextView::class.java))
            )
        ).perform(click())
    }

    @Test
    fun screenshot_SelectProductDelete() {
        getActivityScenario().use {
            selectShoppingListItem("Apples")
            openCabOverflowMenu()
            takeScreenshot("alr-090-select-product-delete")
        }
    }

    private fun clickDeleteShoppingListItem() {
        onView(withText(R.string.delete))
            .perform(click())
    }

    @Test
    fun screenshot_DeleteProduct() {
        getActivityScenario().use {
            selectShoppingListItem("Apples")
            openCabOverflowMenu()
            clickDeleteShoppingListItem()
            takeScreenshot("alr-100-delete-product")
        }
    }

    private fun clickCopyProduct() {
        onView(withText(android.R.string.copy))
            .perform(click())
    }

    @Test
    fun screenshot_CopyProduct() {
        getActivityScenario().use {
            selectShoppingListItem("Apples")
            openCabOverflowMenu()
            clickCopyProduct()
            takeScreenshot("alr-105-copy-product", 300)
        }
    }

    private fun clickShoppingListAislePicker() {
        onView(withId(R.id.mnu_aisle_picker))
            .perform(click())
    }

    @Test
    fun screenshot_ShoppingListAislePicker() {
        getActivityScenario().use {
            selectShoppingListItem("Apples")
            clickShoppingListAislePicker()
            takeScreenshot("alr-107-010-shopping-list-aisle-picker")
        }
    }

    @Test
    fun screenshot_AddAisle() {
        getActivityScenario().use {
            clickFab(FabHandler.FabOption.ADD_AISLE)
            takeScreenshot("alr-110-add-aisle")
        }
    }

    @Test
    fun screenshot_SelectAisle() {
        getActivityScenario().use {
            selectShoppingListItem("Pantry")
            takeScreenshot("alr-120-select-aisle")
        }
    }

    @Test
    fun screenshot_EditAisle() {
        getActivityScenario().use {
            selectShoppingListItem("Pantry")
            clickEditShoppingListItem()
            takeScreenshot("alr-130-edit-aisle")
        }
    }

    @Test
    fun screenshot_SelectAisleDelete() {
        getActivityScenario().use {
            selectShoppingListItem("Pantry")
            openCabOverflowMenu()
            takeScreenshot("alr-140-select-aisle-delete")
        }
    }

    @Test
    fun screenshot_DeleteAisle() {
        getActivityScenario().use {
            selectShoppingListItem("Pantry")
            openCabOverflowMenu()
            clickDeleteShoppingListItem()
            takeScreenshot("alr-150-delete-aisle")
        }
    }

    @Test
    fun screenshot_AddShop() {
        getActivityScenario().use {
            clickFab(FabHandler.FabOption.ADD_SHOP)
            takeScreenshot("alr-160-add-shop")
        }
    }

    @Test
    fun screenshot_ShopExtraOptions() {
        getActivityScenario().use {
            clickFab(FabHandler.FabOption.ADD_SHOP)
            Espresso.closeSoftKeyboard()
            onView(withId(R.id.txt_toggle_extra_options))
                .perform(click())

            takeScreenshot("alr-165-shop-extra-options")
        }
    }

    private fun navigateToAllLists() {
        openNavigationDrawer()
        onView(withId(R.id.nav_all_lists))
            .perform(click())
    }

    private fun selectShop(shopName: String) {
        onView(
            allOf(
                withText(shopName),
                withId(R.id.txt_shop_name),
                isDescendantOfA(withId(R.id.shop_list))
            )
        ).perform(longClick())
    }

    @Test
    fun screenshot_SelectShop() {
        getActivityScenario().use {
            navigateToAllLists()
            selectShop(saveBigSuper)
            takeScreenshot("alr-170-select-shop")
        }
    }

    private fun clickEditShopListItem() {
        onView(withId(R.id.mnu_edit_shop_list_item))
            .perform(click())
    }

    @Test
    fun screenshot_EditShop() {
        getActivityScenario().use {
            navigateToAllLists()
            selectShop(saveBigSuper)
            clickEditShopListItem()
            takeScreenshot("alr-180-edit-shop")
        }
    }

    @Test
    fun screenshot_SelectShopDelete() {
        getActivityScenario().use {
            navigateToAllLists()
            selectShop(saveBigSuper)
            openCabOverflowMenu()
            takeScreenshot("alr-190-select-shop-delete")
        }
    }

    private fun clickDeleteShopListItem() {
        onView(withText(R.string.delete))
            .perform(click())
    }

    @Test
    fun screenshot_DeleteShop() {
        getActivityScenario().use {
            navigateToAllLists()
            selectShop(saveBigSuper)
            openCabOverflowMenu()
            clickDeleteShopListItem()
            takeScreenshot("alr-200-delete-shop")
        }
    }

    private fun clickCopyShop() {
        onView(withText(android.R.string.copy))
            .perform(click())
    }

    @Test
    fun screenshot_CopyShop() {
        getActivityScenario().use {
            navigateToAllLists()
            selectShop(saveBigSuper)
            openCabOverflowMenu()
            clickCopyShop()
            takeScreenshot("alr-185-copy-shop", 300)

        }
    }

    private fun navigateToAllItemsList(@IdRes homeListId: Int) {
        openNavigationDrawer()
        onView(withId(homeListId))
            .perform(click())
    }

    @Test
    fun screenshot_AllItemsList() {
        getActivityScenario().use {
            navigateToAllItemsList(R.id.nav_all_items)
            takeScreenshot("alr-210-all-items-list")
        }
    }

    private fun activateSearchBox() {
        val actionMenuItemView = onView(
            allOf(
                withId(R.id.action_search), withContentDescription("Search"),
                isDisplayed()
            )
        )
        actionMenuItemView.perform(click())
    }

    private fun getSearchTextBox(): ViewInteraction = onView(
        allOf(
            withId(SystemIds.SEARCH_BOX),
            isDisplayed()
        )
    )

    private fun performSearch(searchString: String) {
        activateSearchBox()
        getSearchTextBox().perform(typeText(searchString))

    }

    @Test
    fun screenshot_Search() {
        getActivityScenario().use {
            performSearch("b")
            takeScreenshot("alr-220-search", 500)
        }
    }

    private fun selectRestoreDatabase() {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val now = LocalDateTime.now()

        val testUri = "aisleron-backup-${now.format(formatter)}.db.sqlite"

        val intent = Intent()
        intent.data = Uri.parse(testUri)
        val result: Instrumentation.ActivityResult =
            Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

        Intents.init()
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)

        onView(withId(SystemIds.PREFERENCE_RECYCLER_VIEW))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.restore_database)),
                    click()
                )
            )

        Intents.release()
    }

    @Test
    fun screenshot_ConfirmRestore() {
        getActivityScenario().use {
            navigateToSettings()
            selectRestoreDatabase()
            takeScreenshot("alr-230-confirm-restore")
        }
    }

    private fun navigateToPinnedShop(shopName: String) {
        openNavigationDrawer()
        onView(withText(shopName))
            .perform(click())
    }

    private fun toggleProductStatus(productName: String) {
        onView(
            allOf(
                withId(R.id.chk_in_stock),
                hasSibling(allOf(withText(productName), withId(R.id.txt_product_name)))
            )
        ).perform(click())
    }

    @Test
    fun screenshot_StatusChangeSnackbar() {
        getActivityScenario().use {
            navigateToPinnedShop(saveBigSuper)
            toggleProductStatus("Toothpaste")
            takeScreenshot("alr-240-status-change-snackbar")
        }
    }

    @Test
    fun screenshot_NeededList() {
        getActivityScenario().use {
            navigateToAllItemsList(R.id.nav_needed)
            takeScreenshot("alr-250-needed-list")
        }
    }

    @Test
    fun screenshot_ShopList() {
        getActivityScenario().use {
            navigateToPinnedShop(saveBigSuper)
            takeScreenshot("alr-260-shop-list")
        }
    }

    private suspend fun setAllProductStatus(inStock: Boolean) {
        get<ProductRepository>().getAll().forEach {
            get<UpdateProductStatusUseCase>().invoke(it.id, inStock)
        }
    }

    @Test
    fun screenshot_ShopListFull() = runTest {
        setAllProductStatus(false)
        getActivityScenario().use {
            navigateToPinnedShop(saveBigSuper)
            takeScreenshot("alr-270-shop-list-full")
        }
    }

    @Test
    fun screenshot_AllShops() = runTest {
        getActivityScenario().use {
            navigateToAllLists()
            takeScreenshot("alr-280-all-lists")
        }
    }

    private fun openToolbarOverflowMenu() {
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
    }

    @Test
    fun screenshot_ShopListMenu() = runTest {
        getActivityScenario().use {
            navigateToPinnedShop(saveBigSuper)
            openToolbarOverflowMenu()
            takeScreenshot("alr-290-shop-list-menu")
        }
    }

    private fun clickProductNote() {
        onView(withId(R.id.mnu_show_note))
            .perform(click())
    }

    private fun enterNoteText(noteText: String) {
        onView(withId(R.id.edt_notes))
            .perform(typeText(noteText))

    }

    @Test
    fun screenshot_NotesDialog() {
        getActivityScenario().use {
            selectShoppingListItem("Apples")
            clickProductNote()
            enterNoteText("The freshest apples are at the farmers market.\n\nAsk for Farmer Brown.")
            takeScreenshot("alr-330-notes-dialog")
        }
    }

    private fun clickMainFab() {
        onView(withId(R.id.fab))
            .perform(click())
    }

    private suspend fun collapseAllAisles() {
        get<AisleRepository>().getAll().forEach {
            get<UpdateAisleExpandedUseCase>().invoke(it.id, false)
        }
    }

    @Test
    fun screenshot_FabButtons() = runTest {
        collapseAllAisles()
        getActivityScenario().use {
            clickMainFab()
            takeScreenshot("alr-900-fab-buttons")
        }
    }

    private suspend fun setProductQuantity(productName: String, qty: Double) {
        get<ProductRepository>().getByName(productName)?.let {
            get<UpdateProductQtyNeededUseCase>().invoke(it.id, qty)
        }
    }

    @Test
    fun screenshot_QtyProduct() = runTest {
        SharedPreferencesInitializer().setTrackingMode(SharedPreferencesInitializer.TrackingMode.QUANTITY)
        setProductQuantity("Bread", 2.0)
        getActivityScenario().use {
            takeScreenshot("alr-340-qty-product")
        }
    }

    @Test
    fun screenshot_QtyChkProduct() = runTest {
        SharedPreferencesInitializer().setTrackingMode(SharedPreferencesInitializer.TrackingMode.CHECKBOX_QUANTITY)
        setProductQuantity("Butter", 4.0)
        getActivityScenario().use {
            navigateToPinnedShop(saveBigSuper)
            takeScreenshot("alr-350-qty-chk-product")
        }
    }

    private fun navigateToAllShops() {
        openNavigationDrawer()
        onView(withId(R.id.nav_all_shops))
            .perform(click())
    }

    @Test
    fun screenshot_AllShopsList() = runTest {
        getActivityScenario().use {
            navigateToAllShops()
            takeScreenshot("alr-360-001-all-shops")
        }
    }

    private fun navigateToAbout() {
        openNavigationDrawer()
        onView(withId(R.id.nav_about))
            .perform(click())
    }

    @Test
    fun screenshot_About() {
        getActivityScenario().use {
            navigateToAbout()
            takeScreenshot("alr-055-about")
        }
    }
}

/**
 * Remaining screenshots
 * -----------------------
 * alr-300-emoji-in-stock
 * alr-310-emoji-needed
 * alr-320-emoji-all-items
 */