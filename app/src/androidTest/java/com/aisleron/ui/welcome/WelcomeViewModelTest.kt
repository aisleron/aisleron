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