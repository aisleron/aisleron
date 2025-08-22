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

package com.aisleron.ui.welcome

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, repositoryModule, useCaseModule)
    )

    @Test
    fun createSampleData_ExceptionRaised_WelcomeUiStateIsError() {
        val exceptionMessage = "Error Creating Sample Data"

        val welcomeViewModel = WelcomeViewModel(
            object : CreateSampleDataUseCase {
                override suspend operator fun invoke() {
                    throw Exception(exceptionMessage)
                }
            },
            get<GetAllProductsUseCase>(),
            TestScope(UnconfinedTestDispatcher())
        )

        welcomeViewModel.createSampleData()

        Assert.assertTrue(welcomeViewModel.welcomeUiState.value is WelcomeViewModel.WelcomeUiState.Error)
        Assert.assertEquals(
            AisleronException.ExceptionCode.GENERIC_EXCEPTION,
            (welcomeViewModel.welcomeUiState.value as WelcomeViewModel.WelcomeUiState.Error).errorCode
        )
        Assert.assertEquals(
            exceptionMessage,
            (welcomeViewModel.welcomeUiState.value as WelcomeViewModel.WelcomeUiState.Error).errorMessage
        )
    }

    @Test
    fun createSampleData_SampleDataCreationExceptionExceptionRaised_WelcomeUiStateIsError() {
        val exceptionMessage = "Error Creating Sample Data"

        val welcomeViewModel = WelcomeViewModel(
            object : CreateSampleDataUseCase {
                override suspend operator fun invoke() {
                    throw AisleronException.SampleDataCreationException(
                        exceptionMessage
                    )
                }
            },
            get<GetAllProductsUseCase>(),
            TestScope(UnconfinedTestDispatcher())
        )

        welcomeViewModel.createSampleData()

        Assert.assertTrue(welcomeViewModel.welcomeUiState.value is WelcomeViewModel.WelcomeUiState.Error)
        Assert.assertEquals(
            AisleronException.ExceptionCode.SAMPLE_DATA_CREATION_EXCEPTION,
            (welcomeViewModel.welcomeUiState.value as WelcomeViewModel.WelcomeUiState.Error).errorCode
        )
        Assert.assertEquals(
            exceptionMessage,
            (welcomeViewModel.welcomeUiState.value as WelcomeViewModel.WelcomeUiState.Error).errorMessage
        )
    }

    @Test
    fun createSampleData_CreationSuccessful_WelcomeUiStateIsSampleDataLoaded() {
        val welcomeViewModel = WelcomeViewModel(
            object : CreateSampleDataUseCase {
                override suspend operator fun invoke() {
                    //Do nothing, just validate that view model works if no error is returned.
                }
            },
            get<GetAllProductsUseCase>(),
            TestScope(UnconfinedTestDispatcher())
        )

        welcomeViewModel.createSampleData()

        Assert.assertEquals(
            WelcomeViewModel.WelcomeUiState.SampleDataLoaded,
            welcomeViewModel.welcomeUiState.value
        )
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_WelcomeViewModelReturned() {
        val welcomeViewModel = WelcomeViewModel(
            get<CreateSampleDataUseCase>(),
            get<GetAllProductsUseCase>(),
        )

        Assert.assertNotNull(welcomeViewModel)
    }

    @Test
    fun clearState_AfterCall_StateIsEmpty() = runTest {
        val viewModel = WelcomeViewModel(
            object : CreateSampleDataUseCase {
                override suspend operator fun invoke() {
                    //Do nothing, just validate that view model works if no error is returned.
                }
            },
            get<GetAllProductsUseCase>(),
            TestScope(UnconfinedTestDispatcher())
        )

        viewModel.createSampleData()
        val stateBefore = viewModel.welcomeUiState.value

        viewModel.clearState()
        val stateAfter = viewModel.welcomeUiState.value

        Assert.assertNotEquals(stateBefore, stateAfter)
        Assert.assertTrue(viewModel.welcomeUiState.value is WelcomeViewModel.WelcomeUiState.Empty)
    }

    @Test
    fun checkForProducts_NoProductsExist_StateIsFalse() = runTest {
        val viewModel = WelcomeViewModel(
            object : CreateSampleDataUseCase {
                override suspend operator fun invoke() {
                    //Do nothing, just validate that view model works if no error is returned.
                }
            },
            get<GetAllProductsUseCase>(),
            TestScope(UnconfinedTestDispatcher())
        )

        viewModel.checkForProducts()

        Assert.assertFalse(viewModel.productsLoaded.value)
    }

    @Test
    fun checkForProducts_ProductsExist_StateIsTrue() = runTest {
        val viewModel = WelcomeViewModel(
            get<CreateSampleDataUseCase>(),
            get<GetAllProductsUseCase>(),
            TestScope(UnconfinedTestDispatcher())
        )

        viewModel.createSampleData()
        viewModel.checkForProducts()

        Assert.assertTrue(viewModel.productsLoaded.value)
    }
}