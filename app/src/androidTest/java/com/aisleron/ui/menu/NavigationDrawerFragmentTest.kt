package com.aisleron.ui.menu

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aisleron.R
import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.ui.KoinTestRule
import com.aisleron.ui.shoplist.ShopListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.core.module.Module
import org.koin.dsl.module

@RunWith(value = Parameterized::class)
class NavigationDrawerFragmentTest(
    private val testName: String,
    private val textViewId: Int,
    private val navTargetId: Int
) {

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getKoinModules(): List<Module> {
        val testUseCases = TestUseCaseProvider(TestDataManager())
        val shopListViewModel = ShopListViewModel(
            getShopsUseCase = testUseCases.getShopsUseCase,
            getPinnedShopsUseCase = testUseCases.getPinnedShopsUseCase,
            removeLocationUseCase = testUseCases.removeLocationUseCase,
            getLocationUseCase = testUseCases.getLocationUseCase,
            TestScope(UnconfinedTestDispatcher())
        )

        return listOf(
            module {
                factory<ShopListViewModel> { shopListViewModel }
            }
        )
    }

    private fun getFragmentScenario(): FragmentScenario<NavigationDrawerFragment> =
        launchFragmentInContainer<NavigationDrawerFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { NavigationDrawerFragment() },
            fragmentArgs = null
        )

    @Test
    fun onClick_textViewClicked_NavigateToTargetView() = runTest {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        getFragmentScenario().onFragment { fragment ->
            navController.setGraph(R.navigation.mobile_navigation)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(textViewId)).perform(ViewActions.click())
        Assert.assertEquals(navTargetId, navController.currentDestination?.id)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("navInStock", R.id.nav_in_stock, R.id.nav_in_stock),
                arrayOf("navNeeded", R.id.nav_needed, R.id.nav_needed),
                arrayOf("navAllItems", R.id.nav_all_items, R.id.nav_all_items),
                arrayOf("navSettings", R.id.nav_settings, R.id.nav_settings),
                arrayOf("navAllShops", R.id.nav_all_shops, R.id.nav_all_shops)
            )
        }
    }
}