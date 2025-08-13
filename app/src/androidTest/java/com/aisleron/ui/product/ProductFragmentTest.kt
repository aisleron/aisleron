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
import androidx.test.uiautomator.UiDevice
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.product.ProductRepository
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

class ProductFragmentTest : KoinTest {
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
        val existingProduct = get<ProductRepository>().getAll().first { it.inStock }
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText(existingProduct.name)))
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

        val product = get<ProductRepository>().getByName(newProductName)

        onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText(newProductName)))
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

        onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText("")))
        Assert.assertFalse(addEditFragmentListener.addEditSuccess)
    }

    @Test
    fun onSaveClick_ExistingProductHasUniqueName_ProductUpdated() = runTest {
        val productRepository = get<ProductRepository>()
        val existingProduct = productRepository.getAll().first()
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        val newProductName = existingProduct.name + " Updated"
        val scenario = getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name))
            .perform(ViewActions.clearText())
            .perform(typeText(newProductName))

        scenario.onFragment {
            val menuItem = getSaveMenuItem(it.requireContext())
            it.onMenuItemSelected(menuItem)
        }

        val updatedProduct = productRepository.get(existingProduct.id)

        onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText(newProductName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(updatedProduct)
        Assert.assertEquals(newProductName, updatedProduct?.name)
    }

    @Test
    fun onSaveClick_InStockChanged_InStockUpdated() = runTest {
        val productRepository = get<ProductRepository>()
        val existingProduct = productRepository.getAll().first { !it.inStock }
        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        val scenario = getFragmentScenario(bundle)

        onView(withId(R.id.chk_product_in_stock)).perform(ViewActions.click())
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
        val existingProduct = get<ProductRepository>().getAll().first()
        val bundle = bundler.makeAddProductBundle()
        val scenario = getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name))
            .perform(ViewActions.clearText())
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

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        try {
            device.setOrientationLandscape()

            onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText(newProductName)))
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
                fabHandler
            )
        Assert.assertNotNull(fragment)
    }


    private fun getSaveMenuItem(context: Context): ActionMenuItem {
        val menuItem = ActionMenuItem(context, 0, R.id.mnu_btn_save, 0, 0, null)
        return menuItem
    }

    private fun getFragmentScenario(bundle: Bundle): FragmentScenario<ProductFragment> {
        val scenario = launchFragmentInContainer<ProductFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                ProductFragment(
                    addEditFragmentListener, applicationTitleUpdateListener, fabHandler
                )
            }
        )

        return scenario
    }


}