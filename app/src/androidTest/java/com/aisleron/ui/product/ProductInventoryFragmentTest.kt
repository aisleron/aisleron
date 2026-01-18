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

package com.aisleron.ui.product

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.R
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.settings.ProductPreferencesTestImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest
import java.text.DecimalFormat
import kotlin.test.assertEquals

class ProductInventoryFragmentTest : KoinTest {
    private lateinit var viewModel: ProductInventoryViewModel

    data class ProductInventoryUiDataTestImpl(
        override val qtyIncrement: Double = 1.0,
        override val unitOfMeasure: String = "g",
        override val trackingMode: TrackingMode = TrackingMode.DEFAULT
    ) : ProductInventoryUiData

    class ProductInventoryViewModelTestImpl() : ProductInventoryViewModel {
        override fun updateQtyIncrement(newIncrement: Double) {
            _uiData.value = _uiData.value.copy(qtyIncrement = newIncrement)
        }

        override fun updateUnitOfMeasure(newUom: String) {
            _uiData.value = _uiData.value.copy(unitOfMeasure = newUom)
        }

        override fun updateTrackingMode(selectedMode: TrackingMode) {
            _uiData.value = _uiData.value.copy(trackingMode = selectedMode)
        }

        private val _uiData = MutableStateFlow(ProductInventoryUiDataTestImpl())
        override val uiData: StateFlow<ProductInventoryUiData> = _uiData
    }

    @Before
    fun setUp() {
        viewModel = ProductInventoryViewModelTestImpl()
    }

    private fun setPadding(view: View) {
        ViewCompat.getRootWindowInsets(view)?.let { windowInsets ->
            val actionBarHeight = view.resources.getDimensionPixelSize(R.dimen.toolbar_height)
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = actionBarHeight + insets.top)
        }
    }

    private fun getFragmentScenario(): FragmentScenario<ProductInventoryFragment> {
        val scenario = launchFragmentInContainer<ProductInventoryFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                ProductInventoryFragment(viewModel)
            }
        )

        scenario.onFragment {
            setPadding(it.requireView())
        }

        return scenario
    }

    private fun getTrackingModeDisplayName(mode: TrackingMode): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val values = context.resources.getStringArray(R.array.tracking_method_values)
        val displayNames = context.resources.getStringArray(R.array.tracking_method_names)

        val index = values.indexOfFirst { it == mode.value }
        return if (index != -1 && index < displayNames.size) {
            displayNames[index]
        } else {
            context.getString(R.string.tracking_default)
        }
    }

    @Test
    fun onCreateView_ProductHasInventoryAttributes_showsInitialState() = runTest {
        getFragmentScenario()

        // Verify initial values
        onView(withId(R.id.edt_tracking_mode)).check(
            matches(
                withText(getTrackingModeDisplayName(viewModel.uiData.value.trackingMode))
            )
        )

        onView(withId(R.id.edt_increment)).check(
            matches(
                withText(
                    String.format("%.3f", viewModel.uiData.value.qtyIncrement).trimEnd('0').trimEnd('.')
                )
            )
        )

        onView(withId(R.id.edt_uom)).check(matches(withText(viewModel.uiData.value.unitOfMeasure)))
    }

    @Test
    fun onTrackingModeChanged_updatesTrackingMode() = runTest {
        getFragmentScenario()

        onView(withId(R.id.edt_tracking_mode)).perform(click())

        // Verify the Dialog Title is displayed
        onView(withText(R.string.tracking_mode)).check(matches(isDisplayed()))

        val newMode = TrackingMode.entries.first { it != viewModel.uiData.value.trackingMode }
        val newModeDisplayName = getTrackingModeDisplayName(newMode)

        onView(withText(newModeDisplayName))
            .inRoot(isDialog()) // Extra safety: tells Espresso to look at the dialog window
            .perform(click())

        assertEquals(newMode, viewModel.uiData.value.trackingMode)
    }

    @Test
    fun onUomChanged_updatesUnitOfMeasure() = runTest {
        getFragmentScenario()

        val newUom = "kg"
        onView(withId(R.id.edt_uom)).perform(replaceText(newUom))

        // Verify the UOM was updated
        assertEquals(newUom, viewModel.uiData.value.unitOfMeasure)
    }

    @Test
    fun onIncrementChanged_updatesQuantityIncrement() = runTest {
        getFragmentScenario()

        val newIncrement = "2.5"
        onView(withId(R.id.edt_increment)).perform(replaceText(newIncrement))

        // Verify the increment was updated
        assertEquals(newIncrement.toDouble(), viewModel.uiData.value.qtyIncrement)
    }

    @Test
    fun validateProductInventoryFragmentLoad() = runTest {
        val koinApp = startKoin {
            modules(listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule))
        }

        try {
            val koin = koinApp.koin
            koin.get<CreateSampleDataUseCase>().invoke()
            val product = koin.get<ProductRepository>().getAll().first { it.inStock }

            val bundler = Bundler()
            val bundle = bundler.makeEditProductBundle(product.id)

            launchFragmentInContainer<ProductFragment>(
                fragmentArgs = bundle,
                themeResId = R.style.Theme_Aisleron,
                instantiate = {
                    ProductFragment(
                        AddEditFragmentListenerTestImpl(),
                        ApplicationTitleUpdateListenerTestImpl(),
                        ProductPreferencesTestImpl().also {
                            it.setShowExtraOptions(true)
                        },
                        FabHandlerTestImpl()
                    )
                }
            )

            onView(withText(R.string.product_tab_inventory)).perform(click())

            // 5. Assertions: Verify Child UI is loaded and showing correct data
            onView(withId(R.id.layout_tracking_mode)).check(matches(isDisplayed()))

            // Verify the formatted text logic we discussed earlier
            // (Assuming the fragment loads the initial value into the EditText)
            val trackingMode = getTrackingModeDisplayName(product.trackingMode)
            onView(withId(R.id.edt_tracking_mode)).check(matches(withText(trackingMode)))

            val qtyIncrement = formatQty(product.qtyIncrement)
            onView(withId(R.id.edt_increment)).check(matches(withText(qtyIncrement)))

            onView(withId(R.id.edt_uom)).check(matches(withText(product.unitOfMeasure)))
        } finally {
            // 6. Clean up Koin manually so it doesn't leak into other tests
            stopKoin()
        }
    }

    private fun formatQty(qty: Double): String =
        DecimalFormat("0.###").format(qty)
}