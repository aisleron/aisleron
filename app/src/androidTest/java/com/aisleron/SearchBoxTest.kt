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

package com.aisleron


import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
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
        SharedPreferencesInitializer().setIsInitialized(true)
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
            withId(com.google.android.material.R.id.search_src_text),
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
        onView(withId(com.google.android.material.R.id.search_src_text)).check(matches(isDisplayed()))
    }

    @Test
    fun onSearchBox_IsExistingProduct_ProductDisplayed() = runTest {
        val product = get<ProductRepository>().getAll().first()
        performSearch(product.name)
        getProductView(product.name).check(matches(isDisplayed()))
    }

    @Test
    fun onSearchBox_IsNonExistentProduct_ProductDisplayed() {
        val searchString = "This is Not a Real Product Name"

        performSearch(searchString)

        getProductView(searchString).check(doesNotExist())
    }

    @Test
    fun onSearchBox_ClearSearchClicked_ShowProducts() = runTest {
        val product = get<ProductRepository>().getAll().first()
        val searchString = "This is Not a Real Product Name"

        performSearch(searchString)
        val clearSearch = onView(
            Matchers.allOf(
                withId(com.google.android.material.R.id.search_close_btn),
                withContentDescription("Clear query"),
                isDisplayed()
            )
        )
        clearSearch.perform(click())

        getProductView(product.name).check(matches(isDisplayed()))
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

    /*
        Fab shows correctly
     */
}

