package com.aisleron.di

import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.ui.product.ProductViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import com.aisleron.ui.shoppinglist.ShoppingListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
            }
        )

        return modules
    }
}