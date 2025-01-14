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
import com.aisleron.data.TestDataManager
import com.aisleron.di.KoinTestRule
import com.aisleron.di.TestAppModules
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module

class SearchBoxTest {

    private lateinit var testData: TestDataManager
    private lateinit var scenario: ActivityScenario<MainActivity>

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
        SharedPreferencesInitializer().invoke(isInitialized = true)
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
    fun onSearchBox_IsExistingProduct_ProductDisplayed() {
        val product = runBlocking { testData.productRepository.getAll().first() }
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
    fun onSearchBox_ClearSearchClicked_ShowProducts() {
        val product = runBlocking { testData.productRepository.getAll().first() }
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

