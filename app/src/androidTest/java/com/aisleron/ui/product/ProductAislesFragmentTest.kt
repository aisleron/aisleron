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

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.GetProductMappingsUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.settings.ProductPreferencesTestImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get

class ProductAislesFragmentTest : KoinTest {
    private lateinit var bundler: Bundler
    private lateinit var addEditFragmentListener: AddEditFragmentListenerTestImpl
    private lateinit var applicationTitleUpdateListener: ApplicationTitleUpdateListenerTestImpl
    private lateinit var product: Product

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        bundler = Bundler()
        addEditFragmentListener = AddEditFragmentListenerTestImpl()
        applicationTitleUpdateListener = ApplicationTitleUpdateListenerTestImpl()
        runBlocking {
            get<CreateSampleDataUseCase>().invoke()
            product = get<ProductRepository>().getAll().first { it.inStock }
        }
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
                    productPreferences ?: ProductPreferencesTestImpl().also {
                        val context = InstrumentationRegistry.getInstrumentation().context
                        it.setShowExtraOptions(context, true)
                    }
                )
            }
        )

        onView(withText(R.string.product_tab_aisles))
            .perform(click())

        return scenario
    }

    @Test
    fun onCreateView_withProductAisles_showsProductAisles() = runTest {
        val mappings = get<GetProductMappingsUseCase>().invoke(product.id)
        val bundle = bundler.makeEditProductBundle(product.id)
        getFragmentScenario(bundle)

        mappings.forEach {
            onView(withText(it.name)).check(matches(isDisplayed()))
            onView(withText(it.aisles.first().name)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun onCreateView_withNoProductAisles_showsEmptyList() = runTest {
        val productId = get<ProductRepository>().add(
            Product(0, "Not Mapped Product", true, 0)
        )

        val bundle = bundler.makeEditProductBundle(productId)
        getFragmentScenario(bundle)

        onView(withId(R.id.product_aisles_list)).check(matches(hasChildCount(1)))
    }

    @Test
    fun clickAisleItem_isLocationWithAisle_showAislePickerDialog() = runTest {
        val location = get<GetProductMappingsUseCase>().invoke(product.id).first()
        val bundle = bundler.makeEditProductBundle(product.id)
        getFragmentScenario(bundle)

        onView(withText(location.name))
            .perform(click())

        onView(withText(location.name))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun onAislePickerResult_selectAisle_updatesProductAisle() = runTest {
        val location = get<GetProductMappingsUseCase>().invoke(product.id).first()
        val aisleToSelect = get<AisleRepository>().getForLocation(location.id)
            .first { it.id != location.aisles.first().id }

        val bundle = bundler.makeEditProductBundle(product.id)
        getFragmentScenario(bundle)

        onView(withText(location.name)).perform(click())
        onView(withText(aisleToSelect.name)).inRoot(isDialog())
            .perform(click())

        onView(allOf(withText(aisleToSelect.name), hasSibling(withText(location.name)))).check(
            matches(isDisplayed())
        )
    }

    /*
        @Test
        fun onAislePickerResult_addNewAisle_doesNothing() {
            val pickerData = AislePickerBundle("Test Dialog", emptyList(), -1)
            launchFragment()

            activityScenarioRule.scenario.onActivity {
                viewModel.aislesForLocationFlow.postValue(pickerData)
            }

            onView(withText(R.string.add_aisle)).inRoot(isDialog()).perform(click())

            assertNull(viewModel.updatedAisleId)

        }*/

}
