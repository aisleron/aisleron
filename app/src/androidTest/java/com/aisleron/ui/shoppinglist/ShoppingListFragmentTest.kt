package com.aisleron.ui.shoppinglist

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.aisleron.R
import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.KoinTestRule
import com.aisleron.ui.TestApplicationTitleUpdateListener
import com.aisleron.ui.bundles.Bundler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module

class ShoppingListFragmentTest {

    private lateinit var bundler: Bundler
    private lateinit var applicationTitleUpdateListener: TestApplicationTitleUpdateListener
    private lateinit var testData: TestDataManager

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getKoinModules(): List<Module> {
        testData = TestDataManager()
        val testUseCases = TestUseCaseProvider(testData)
        val shoppingListViewModel = ShoppingListViewModel(
            testUseCases.getShoppingListUseCase,
            testUseCases.updateProductStatusUseCase,
            testUseCases.addAisleUseCase,
            testUseCases.updateAisleUseCase,
            testUseCases.updateAisleProductRankUseCase,
            testUseCases.updateAisleRankUseCase,
            testUseCases.removeAisleUseCase,
            testUseCases.removeProductUseCase,
            testUseCases.getAisleUseCase,
            TestScope(UnconfinedTestDispatcher())
        )

        return listOf(
            module {
                factory<ShoppingListViewModel> { shoppingListViewModel }
            }
        )
    }

    private fun getFragmentScenario(bundle: Bundle): FragmentScenario<ShoppingListFragment> {
        val scenario = launchFragmentInContainer<ShoppingListFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_Aisleron,
            instantiate = { ShoppingListFragment(applicationTitleUpdateListener) }
        )
        return scenario
    }

    private fun getLocation(locationType: LocationType): Location =
        runBlocking {
            testData.locationRepository.getAll().first { it.type == locationType }
        }

    @Before
    fun setUp() {
        bundler = Bundler()
        applicationTitleUpdateListener = TestApplicationTitleUpdateListener()
    }

    @Test
    fun newInstance_CallNewInstance_ReturnsFragment() {
        val fragment =
            ShoppingListFragment.newInstance(applicationTitleUpdateListener, 1, FilterType.ALL)
        Assert.assertNotNull(fragment)
    }

    @Test
    fun onCreateShoppingListFragment_HomeFilterIsInStock_AppTitleIsInStock() {
        val location = getLocation(LocationType.HOME)
        val bundle = bundler.makeShoppingListBundle(location.id, FilterType.IN_STOCK)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.menu_in_stock),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShoppingListFragment_HomeFilterIsNeeded_AppTitleIsShoppingList() {
        val location = getLocation(LocationType.HOME)
        val bundle = bundler.makeShoppingListBundle(location.id, FilterType.NEEDED)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.menu_shopping_list),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShoppingListFragment_HomeFilterIsAll_AppTitleIsShoppingList() {
        val location = getLocation(LocationType.HOME)
        val bundle = bundler.makeShoppingListBundle(location.id, FilterType.ALL)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.menu_all_items),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShoppingListFragment_LocationTypeIsShop_AppTitleIsShopName() {
        val location = getLocation(LocationType.SHOP)
        val bundle = bundler.makeShoppingListBundle(location.id, location.defaultFilter)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                location.name,
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateShoppingListFragment_BundleISAttributes_FragmentCreated() {
        val location = getLocation(LocationType.HOME)
        val bundle = Bundle()
        bundle.putInt("locationId", location.id)
        bundle.putSerializable("filterType", FilterType.NEEDED)
        val scenario = getFragmentScenario(bundle)
        scenario.onFragment {
            Assert.assertEquals(
                it.getString(R.string.menu_shopping_list),
                applicationTitleUpdateListener.appTitle
            )
        }
    }

    @Test
    fun onCreateView() {
    }

    @Test
    fun onViewCreated() {
    }

    @Test
    fun onQueryTextSubmit() {
    }

    @Test
    fun onQueryTextChange() {
    }

    @Test
    fun onCreateActionMode() {
    }

    @Test
    fun onPrepareActionMode() {
    }

    @Test
    fun onActionItemClicked() {
    }

    @Test
    fun onDestroyActionMode() {
    }
}