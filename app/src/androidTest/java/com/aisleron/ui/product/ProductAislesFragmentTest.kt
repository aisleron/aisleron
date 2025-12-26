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
import android.widget.EditText
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
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
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.GetProductMappingsUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.settings.ProductPreferencesTestImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
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
                    },

                    FabHandlerTestImpl()
                )
            }
        )

        onView(withText(R.string.product_tab_aisles)).perform(click())

        return scenario
    }

    @Test
    fun onCreateView_withProductAisles_showsProductAisles() = runTest {
        val mappings = get<GetProductMappingsUseCase>().invoke(product.id)
        val bundle = bundler.makeEditProductBundle(product.id)
        getFragmentScenario(bundle)

        mappings.forEach {
            onView(
                allOf(
                    withText(it.aisles.first().name),
                    hasSibling(withText(it.name))
                )
            ).check(matches(isDisplayed()))
        }
    }

    @Test
    fun onCreateView_onAddProduct_showsDefaultAisles() = runTest {
        val mappings = get<GetProductMappingsUseCase>().invoke(-1)

        val bundle = bundler.makeAddProductBundle("Not Mapped Product")
        getFragmentScenario(bundle)

        mappings.forEach {
            onView(
                allOf(
                    withText(it.aisles.first().name),
                    hasSibling(withText(it.name))
                )
            ).check(matches(isDisplayed()))
        }
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

    @Test
    fun onResume_LocationAdded_ShowsNewLocation() = runTest {
        val bundle = bundler.makeEditProductBundle(product.id)
        val scenario = getFragmentScenario(bundle)
        val newLocationName = "A New Location"
        get<AddLocationUseCase>().invoke(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = newLocationName,
                pinned = false,
                aisles = emptyList(),
                showDefaultAisle = true
            )
        )

        scenario.moveToState(Lifecycle.State.STARTED)
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withText(newLocationName)).check(matches(isDisplayed()))
    }

    @Test
    fun onAislePickerResult_addNewAisle_showAisleDialog() = runTest {
        val location = get<GetProductMappingsUseCase>().invoke(product.id).first()
        val bundle = bundler.makeEditProductBundle(product.id)
        getFragmentScenario(bundle)

        onView(withText(location.name)).perform(click())
        onView(withText(R.string.new_aisle)).inRoot(isDialog())
            .perform(click())

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withText(R.string.add_another))
            .inRoot(isDialog())
            .check(doesNotExist())
    }

    @Test
    fun onAddNewAisleResult_isValidAisle_updatesProductAisle() = runTest {
        val location = get<GetProductMappingsUseCase>().invoke(product.id).first()
        val bundle = bundler.makeEditProductBundle(product.id)
        getFragmentScenario(bundle)

        onView(withText(location.name)).perform(click())
        onView(withText(R.string.new_aisle)).inRoot(isDialog())
            .perform(click())

        val newAisleName = "New Aisle"
        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        onView(allOf(withText(newAisleName), hasSibling(withText(location.name)))).check(
            matches(isDisplayed())
        )
    }
}
