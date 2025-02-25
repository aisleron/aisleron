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

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Test

class WelcomeViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createSampleData_ExceptionRaised_WelcomeUiStateIsError() {
        val exceptionMessage = "Error Creating Sample Data"

        val welcomeViewModel = WelcomeViewModel(
            object : CreateSampleDataUseCase {
                override suspend operator fun invoke() {
                    throw Exception(exceptionMessage)
                }
            },
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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createSampleData_CreationSuccessful_WelcomeUiStateIsSampleDataLoaded() {
        val welcomeViewModel = WelcomeViewModel(
            object : CreateSampleDataUseCase {
                override suspend operator fun invoke() {
                    //Do nothing, just validate that view model works if no error is returned.
                }
            },
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
            object : CreateSampleDataUseCase {
                override suspend operator fun invoke() {}
            }
        )

        Assert.assertNotNull(welcomeViewModel)
    }
}