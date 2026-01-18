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

package com.aisleron


import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerImpl
import com.aisleron.utils.SystemIds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare

class SearchBoxTest : KoinTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule,
            fragmentModule,
            viewModelTestModule,
            repositoryModule,
            useCaseModule,
            generalTestModule,
            preferenceTestModule
        )
    )

    @Before
    fun setUp() {
        declare<FabHandler> { FabHandlerImpl(get()) }
        SharedPreferencesInitializer().apply {
            clearPreferences()
            setIsInitialized(true)
            setLastUpdateCode(BuildConfig.VERSION_CODE)
            setLastUpdateName(BuildConfig.VERSION_NAME)
        }

        runBlocking { get<CreateSampleDataUseCase>().invoke() }
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
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
        getSearchTextBox().perform(typeText(searchString), closeSoftKeyboard())

    }

    private fun getProductView(searchString: String): ViewInteraction =
        onView(allOf(withText(searchString), withId(R.id.txt_product_name)))

    @Test
    fun onSearchClick_SearchBoxDisplayed() {
        activateSearchBox()
        onView(withId(SystemIds.SEARCH_BOX)).check(matches(isDisplayed()))
    }

    @Test
    fun onSearchBox_IsExistingProduct_ProductDisplayed() = runTest {
        val product = get<ProductRepository>().getAll().first()
        performSearch(product.name)
        getProductView(product.name).check(matches(isDisplayed()))
    }

    @Test
    fun onSearchBox_IsNonExistentProduct_ProductNotDisplayed() {
        val searchString = "This is Not a Real Product Name"

        performSearch(searchString)

        getProductView(searchString).check(doesNotExist())
    }

    @Test
    fun onSearchBox_ClearSearchClicked_DonNotRunSearch() = runTest {
        val product = get<ProductRepository>().getAll().first()
        val searchString = "This is Not a Real Product Name"

        performSearch(searchString)
        val clearSearch = onView(
            Matchers.allOf(
                withId(SystemIds.SEARCH_CLOSE_BTN),
                isDisplayed()
            )
        )
        clearSearch.perform(click())

        getProductView(product.name).check(doesNotExist())
    }

    @Test
    fun onSearchBox_BackPressed_SearchBoxHidden() {
        val searchString = "This is Not a Real Product Name"

        performSearch(searchString)
        val backAction = onView(
            Matchers.allOf(withContentDescription("Collapse"), isDisplayed())
        )
        backAction.perform(click())
        Thread.sleep(500)

        getSearchTextBox().check(doesNotExist())
    }

    @Test
    fun onSearchBox_NavigateAwayAndBack_SearchDismissedAndDefaultListShown() {
        val existingProduct = "Frozen Vegetables"
        getProductView(existingProduct).check(matches(isDisplayed()))

        val searchString = "Ap"
        performSearch(searchString)
        getProductView(existingProduct).check(doesNotExist())

        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.fab_add_product)).perform(click())
        Espresso.closeSoftKeyboard()
        Espresso.pressBack()

        getSearchTextBox().check(doesNotExist())

        scrollToRecyclerViewProduct(existingProduct)
        getProductView(existingProduct).check(matches(isDisplayed()))
    }

    private fun scrollToRecyclerViewProduct(productName: String) {
        onView(withId(R.id.frg_shopping_list))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(
                        allOf(
                            withText(productName),
                            withId(R.id.txt_product_name))
                    )
                )
            )
    }
}

