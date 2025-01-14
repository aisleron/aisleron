package com.aisleron.di

import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.product.ProductFragment
import com.aisleron.ui.product.ProductViewModel
import com.aisleron.ui.settings.DisplayPreferences
import com.aisleron.ui.settings.DisplayPreferencesImpl
import com.aisleron.ui.settings.SettingsViewModel
import com.aisleron.ui.settings.ShoppingListPreferences
import com.aisleron.ui.settings.ShoppingListPreferencesTestImpl
import com.aisleron.ui.settings.WelcomePreferences
import com.aisleron.ui.settings.WelcomePreferencesImpl
import com.aisleron.ui.shop.ShopFragment
import com.aisleron.ui.shop.ShopViewModel
import com.aisleron.ui.shoplist.ShopListFragment
import com.aisleron.ui.shoplist.ShopListViewModel
import com.aisleron.ui.shoppinglist.ShoppingListFragment
import com.aisleron.ui.shoppinglist.ShoppingListViewModel
import com.aisleron.ui.welcome.WelcomeFragment
import com.aisleron.ui.welcome.WelcomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.androidx.fragment.dsl.fragment
import org.koin.core.module.Module
import org.koin.dsl.module

class TestAppModules {

    fun getTestAppModules(testData: TestDataManager): List<Module> {
        val testUseCases = TestUseCaseProvider(testData)

        @OptIn(ExperimentalCoroutinesApi::class)
        val modules = mutableListOf(
            module {
                factory<ShoppingListViewModel> {
                    ShoppingListViewModel(
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
                }

                factory<ShopListViewModel> {
                    ShopListViewModel(
                        getShopsUseCase = testUseCases.getShopsUseCase,
                        getPinnedShopsUseCase = testUseCases.getPinnedShopsUseCase,
                        removeLocationUseCase = testUseCases.removeLocationUseCase,
                        getLocationUseCase = testUseCases.getLocationUseCase,
                        TestScope(UnconfinedTestDispatcher())
                    )
                }

                factory<ProductViewModel> {
                    ProductViewModel(
                        addProductUseCase = testUseCases.addProductUseCase,
                        updateProductUseCase = testUseCases.updateProductUseCase,
                        getProductUseCase = testUseCases.getProductUseCase,
                        TestScope(UnconfinedTestDispatcher())
                    )
                }

                factory<ShopViewModel> {
                    ShopViewModel(
                        addLocationUseCase = testUseCases.addLocationUseCase,
                        updateLocationUseCase = testUseCases.updateLocationUseCase,
                        getLocationUseCase = testUseCases.getLocationUseCase,
                        TestScope(UnconfinedTestDispatcher())
                    )
                }

                factory<SettingsViewModel> {
                    SettingsViewModel(
                        backupDatabaseUseCase = testUseCases.backupDatabaseUseCase,
                        restoreDatabaseUseCase = testUseCases.restoreDatabaseUseCase,
                        TestScope(UnconfinedTestDispatcher())
                    )
                }

                factory<WelcomeViewModel> {
                    WelcomeViewModel(
                        createSampleDataUseCase = testUseCases.createSampleDataUseCase,
                        TestScope(UnconfinedTestDispatcher())
                    )
                }

                //ToDo: Test Implementations for all preferences
                factory<DisplayPreferences> { DisplayPreferencesImpl() }

                factory<ShoppingListPreferences> { ShoppingListPreferencesTestImpl() }

                factory<WelcomePreferences> { WelcomePreferencesImpl() }

                factory<FabHandler> { FabHandlerTestImpl() }

                factory<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerTestImpl() }
            },
            module {
                fragment {
                    ShoppingListFragment(
                        applicationTitleUpdateListener = get(),
                        fabHandler = get(),
                        shoppingListPreferences = get()
                    )
                }

                fragment {
                    ProductFragment(
                        null,
                        applicationTitleUpdateListener = get(),
                        fabHandler = get()
                    )
                }

                fragment {
                    ShopFragment(
                        null,
                        applicationTitleUpdateListener = get(),
                        fabHandler = get()
                    )
                }

                fragment { ShopListFragment(fabHandler = get()) }

                fragment { WelcomeFragment(fabHandler = get(), welcomePreferences = get()) }
            }
        )

        return modules
    }
}