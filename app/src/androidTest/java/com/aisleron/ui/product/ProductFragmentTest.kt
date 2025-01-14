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
import com.aisleron.R
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.di.TestAppModules
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module

class ProductFragmentTest {
    private lateinit var bundler: Bundler
    private lateinit var addEditFragmentListener: AddEditFragmentListenerTestImpl
    private lateinit var applicationTitleUpdateListener: ApplicationTitleUpdateListenerTestImpl
    private lateinit var testData: TestDataManager
    private lateinit var fabHandler: FabHandlerTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> {
        testData = TestDataManager()
        return TestAppModules().getTestAppModules(testData)
    }

    @Before
    fun setUp() {
        bundler = Bundler()
        addEditFragmentListener = AddEditFragmentListenerTestImpl()
        applicationTitleUpdateListener = ApplicationTitleUpdateListenerTestImpl()
        fabHandler = FabHandlerTestImpl()
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
    fun onCreateProductFragment_HasEditBundle_ScreenMatchesEditProduct() {
        val existingProduct = runBlocking {
            testData.productRepository.getAll().first { it.inStock }
        }

        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        getFragmentScenario(bundle)

        onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText(existingProduct.name)))
        onView(withId(R.id.chk_product_in_stock)).check(matches(ViewMatchers.isChecked()))
    }

    @Test
    fun onCreateProductFragment_HasAddBundle_AppTitleIsAdd() {
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
    fun onSaveClick_NewProductHasUniqueName_ProductSaved() {
        val bundle = bundler.makeAddProductBundle("New Product")
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()
        val newProductName = "Product Add New Test"

        onView(withId(R.id.edt_product_name)).perform(typeText(newProductName))
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val product = runBlocking {
            testData.productRepository.getByName(newProductName)
        }

        onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText(newProductName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(product)
    }

    @Test
    fun onSaveClick_NoProductNameEntered_DoNothing() {
        val bundle = bundler.makeAddProductBundle()
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()

        scenario.onFragment {
            it.onMenuItemSelected(menuItem)
        }

        onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText("")))
        Assert.assertFalse(addEditFragmentListener.addEditSuccess)
    }

    @Test
    fun onSaveClick_ExistingProductHasUniqueName_ProductUpdated() {
        val existingProduct = runBlocking {
            testData.productRepository.getAll().first()
        }

        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()
        val newProductName = existingProduct.name + " Updated"

        onView(withId(R.id.edt_product_name))
            .perform(ViewActions.clearText())
            .perform(typeText(newProductName))
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val updatedProduct = runBlocking { testData.productRepository.get(existingProduct.id) }

        onView(withId(R.id.edt_product_name)).check(matches(ViewMatchers.withText(newProductName)))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertNotNull(updatedProduct)
        Assert.assertEquals(newProductName, updatedProduct?.name)
    }

    @Test
    fun onSaveClick_InStockChanged_InStockUpdated() {
        val existingProduct = runBlocking {
            testData.productRepository.getAll().first { !it.inStock }
        }

        val bundle = bundler.makeEditProductBundle(existingProduct.id)
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()

        onView(withId(R.id.chk_product_in_stock)).perform(ViewActions.click())
        scenario.onFragment { it.onMenuItemSelected(menuItem) }

        val updatedProduct = runBlocking { testData.productRepository.get(existingProduct.id) }

        onView(withId(R.id.chk_product_in_stock)).check(matches(ViewMatchers.isChecked()))
        Assert.assertTrue(addEditFragmentListener.addEditSuccess)
        Assert.assertEquals(
            existingProduct.copy(inStock = !existingProduct.inStock),
            updatedProduct
        )
    }

    @Test
    fun onSaveClick_IsDuplicateName_ShowErrorSnackBar() {
        val existingProduct = runBlocking {
            testData.productRepository.getAll().first()
        }

        val bundle = bundler.makeAddProductBundle()
        val scenario = getFragmentScenario(bundle)
        val menuItem = getSaveMenuItem()

        onView(withId(R.id.edt_product_name))
            .perform(ViewActions.clearText())
            .perform(typeText(existingProduct.name))
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
        val fragment =
            ProductFragment.newInstance(null, false, addEditFragmentListener)
        Assert.assertNotNull(fragment)
    }


    private fun getSaveMenuItem(): ActionMenuItem {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
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