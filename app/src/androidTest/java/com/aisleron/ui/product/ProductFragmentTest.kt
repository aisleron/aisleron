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

package com.aisleron.ui.product

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.view.menu.ActionMenuItem
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
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
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.settings.ProductPreferencesTestImpl
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

class ProductFragmentTest : KoinTest {
    private lateinit var bundler: Bundler
    private lateinit var addEditFragmentListener: AddEditFragmentListenerTestImpl
    private lateinit var applicationTitleUpdateListener: ApplicationTitleUpdateListenerTestImpl
    private lateinit var fabHandler: FabHandlerTestImpl
    private lateinit var productRepository: ProductRepository

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        bundler = Bundler()
        addEditFragmentListener = AddEditFragmentListenerTestImpl()
        applicationTitleUpdateListener = ApplicationTitleUpdateListenerTestImpl()
        productRepository = get<ProductRepository>()
        fabHandler = FabHandlerTestImpl()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun onCreateProductFragment_HasEditBundle_AppTitleIsEdit() {
        val bundle = bundler.makeEditProductBundle(1)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.edit_product),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateProductFragment_HasEditBundle_ScreenMatchesEditProduct() = runTest {
        val existingProduct = productRepository.getAll().first { it.inStock }
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name)).check(matches(withText(existingProduct.name)))
        onView(withId(R.id.chk_product_in_stock)).check(matches(ViewMatchers.isChecked()))
    }

    @Test
    fun onCreateProductFragment_HasAddBundle_AppTitleIsAdd() = runTest {
        val bundle = bundler.makeAddProductBundle("New Product")
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.add_product),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onSaveClick_NewProductHasUniqueName_ProductSaved() = runTest {
        val bundle = bundler.makeAddProductBundle("New Product")
        val newProductName = "Product Add New Test"
        val scenario = getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name)).perform(typeText(newProductName))
        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val product = productRepository.getByName(newProductName)

        onView(withId(R.id.edt_product_name)).check(matches(withText(newProductName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(product)
    }

    @Test
    fun onSaveClick_NoProductNameEntered_DoNothing() = runTest {
        val bundle = bundler.makeAddProductBundle()
        val scenario = getFragmentScenario(bundle)

        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        onView(withId(R.id.edt_product_name)).check(matches(withText("")))
        Assert.assertFalse(addEditFragmentListener.addEditSuccess)
    }

    @Test
    fun onSaveClick_ExistingProductHasUniqueName_ProductUpdated() = runTest {
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        val newProductName = existingProduct.name + " Updated"
        val scenario = getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name))
            .perform(clearText())
            .perform(typeText(newProductName))

        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val updatedProduct = productRepository.get(existingProduct.id)

        onView(withId(R.id.edt_product_name)).check(matches(withText(newProductName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(updatedProduct)
        Assert.assertEquals(newProductName, updatedProduct?.name)
    }

    @Test
    fun onSaveClick_InStockChanged_InStockUpdated() = runTest {
        val existingProduct = productRepository.getAll().first { !it.inStock }
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        val scenario = getFragmentScenario(bundle)

        onView(withId(R.id.chk_product_in_stock)).perform(click())
        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val updatedProduct = productRepository.get(existingProduct.id)

        onView(withId(R.id.chk_product_in_stock)).check(matches(ViewMatchers.isChecked()))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertEquals(
            existingProduct.copy(inStock = !existingProduct.inStock),
            updatedProduct
        )
    }

    @Test
    fun onSaveClick_IsDuplicateName_ShowErrorSnackBar() = runTest {
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeAddProductBundle()
        val scenario = getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name))
            .perform(clearText())
            .perform(typeText(existingProduct.name))

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
    fun onRotateDevice_ProductDetailsChanged_ProductDetailsPersist() = runTest {
        val bundle = bundler.makeAddProductBundle("New Product")
        val newProductName = "Product Add New Test"
        getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name)).perform(typeText(newProductName))

        val device = UiDevice.getInstance(getInstrumentation())

        try {
            device.setOrientationLandscape()

            onView(withId(R.id.edt_product_name)).check(matches(withText(newProductName)))
        } finally {
            device.setOrientationPortrait()
        }
    }

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() = runTest {
        val fragment =
            ProductFragment.newInstance(
                null,
                false,
                addEditFragmentListener,
                applicationTitleUpdateListener,
                ProductPreferencesTestImpl(),
                fabHandler
            )
        Assert.assertNotNull(fragment)
    }

    @Test
    fun onCreateView_PreferenceIsHideExtraOption_ExtraOptionsGone() = runTest {
        val preferences = ProductPreferencesTestImpl()
        preferences.setShowExtraOptions(getInstrumentation().targetContext, false)
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)

        getFragmentScenario(bundle, preferences)

        onView(withId(R.id.txt_toggle_extra_options))
            .check(matches(withText(R.string.extra_options)))

        onView(withId(R.id.layout_extra_options)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)
            )
        )
    }

    private fun getShowExtraOptionsPreference(showExtraOptions: Boolean): ProductPreferencesTestImpl {
        val preferences = ProductPreferencesTestImpl()
        preferences.setShowExtraOptions(getInstrumentation().targetContext, showExtraOptions)
        return preferences
    }

    @Test
    fun onCreateView_PreferenceIsShowExtraOption_ExtraOptionsVisible() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)

        getFragmentScenario(bundle, preferences)

        onView(withId(R.id.layout_extra_options)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        )
    }

    @Test
    fun onClickExtrasToggle_ExtraOptionsIsGone_ExtraOptionsVisible() = runTest {
        val preferences = getShowExtraOptionsPreference(false)
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        getFragmentScenario(bundle, preferences)

        onView(withId(R.id.txt_toggle_extra_options))
            .perform(click())

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
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)

        getFragmentScenario(bundle, preferences)
        onView(withId(R.id.txt_toggle_extra_options))
            .perform(click())

        onView(withId(R.id.layout_extra_options)).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)
            )
        )

        val endPreference = preferences.showExtraOptions(getInstrumentation().targetContext)
        assertFalse(endPreference)
    }

    @Test
    fun onCreateView_ProductHasNoNote_NoteEmpty() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)

        getFragmentScenario(bundle, preferences)
        onView(
            allOf(
                isDescendantOfA(withId(R.id.tab_product_options)),
                withText(R.string.tab_notes)
            )
        ).perform(click())

        onView(withId(R.id.edt_notes)).check(
            matches(withText(emptyString()))
        )
    }

    @Test
    fun onCreateView_ProductHasNote_NoteDisplayed() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val existingProduct = productRepository.getAll().first()
        val noteText = "Test note displayed on product"
        val noteId = get<NoteRepository>().add(Note(0, noteText))
        productRepository.update(existingProduct.copy(noteId = noteId))
        val bundle = bundler.makeEditProductBundle(existingProduct.id)

        getFragmentScenario(bundle, preferences)
        onView(
            allOf(
                isDescendantOfA(withId(R.id.tab_product_options)),
                withText(R.string.tab_notes)
            )
        ).perform(click())

        onView(withId(R.id.edt_notes)).check(
            matches(withText(noteText))
        )
    }

    @Test
    fun onSaveClick_NoteEntered_NoteSaved() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        val scenario = getFragmentScenario(bundle, preferences)

        onView(
            allOf(
                isDescendantOfA(withId(R.id.tab_product_options)),
                withText(R.string.tab_notes)
            )
        ).perform(click())

        val noteText = "Note added to product"
        onView(withId(R.id.edt_notes))
            .perform(clearText())
            .perform(typeText(noteText))

        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val note = get<NoteRepository>().getAll().firstOrNull { it.noteText == noteText }
        assertNotNull(note)

        val updatedProduct = productRepository.get(existingProduct.id)
        assertEquals(note.id, updatedProduct?.noteId)
    }

    private fun switchToExtrasTab(@StringRes tabNameResId: Int) {
        onView(withText(tabNameResId)).perform(click())
    }

    @Test
    fun onExtraOptions_onTabSelection_AddShopFabShowsCorrectly() = runTest {
        val preferences = getShowExtraOptionsPreference(true)
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        getFragmentScenario(bundle, preferences)

        // Fab is hidden on notes tab
        switchToExtrasTab(R.string.tab_notes)
        assertEquals(0, fabHandler.getFabItems().size)

        // Fab is displayed when switching to the Aisle tab
        switchToExtrasTab(R.string.product_tab_aisles)
        assertEquals(1, fabHandler.getFabItems().size)
        assertEquals(FabHandler.FabOption.ADD_SHOP, fabHandler.getFabItems().first())

        // Fab is hidden when collapsing extra options
        onView(withId(R.id.txt_toggle_extra_options)).perform(click())
        assertEquals(0, fabHandler.getFabItems().size)

        // Fab is shown again when expanding extra options and aisle is the active tab
        onView(withId(R.id.txt_toggle_extra_options)).perform(click())
        assertEquals(1, fabHandler.getFabItems().size)
        assertEquals(FabHandler.FabOption.ADD_SHOP, fabHandler.getFabItems().first())

        // Fab is hidden again when switching back to notes tab
        switchToExtrasTab(R.string.tab_notes)
        assertEquals(0, fabHandler.getFabItems().size)

        //Fab is hidden when switching to the inventory tab
        switchToExtrasTab(R.string.product_tab_aisles)
        assertEquals(1, fabHandler.getFabItems().size)

        switchToExtrasTab(R.string.product_tab_inventory)
        assertEquals(0, fabHandler.getFabItems().size)
    }

    private fun getSaveMenuItem(context: Context): ActionMenuItem {
        val menuItem = ActionMenuItem(context, 0, R.id.mnu_btn_save, 0, 0, null)
        return menuItem
    }

    private fun getFragmentScenario(
        bundle: Bundle, productPreferences: ProductPreferencesTestImpl? = null
    ): FragmentScenario<ProductFragment> {
        val scenario = launchFragmentInContainer<ProductFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                ProductFragment(
                    addEditFragmentListener,
                    applicationTitleUpdateListener,
                    productPreferences ?: ProductPreferencesTestImpl(),
                    fabHandler
                )
            }
        )

        return scenario
    }
}