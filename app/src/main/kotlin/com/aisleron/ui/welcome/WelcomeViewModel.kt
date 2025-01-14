package com.aisleron.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val createSampleDataUseCase: CreateSampleDataUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private val _welcomeUiState = MutableStateFlow<WelcomeUiState>(
        WelcomeUiState.Empty
    )

    val welcomeUiState = _welcomeUiState.asStateFlow()

    fun createSampleData() {
        coroutineScope.launch {
            try {
                createSampleDataUseCase()
                _welcomeUiState.value = WelcomeUiState.SampleDataLoaded
            } catch (e: AisleronException) {
                _welcomeUiState.value = WelcomeUiState.Error(e.exceptionCode, e.message)

            } catch (e: Exception) {
                _welcomeUiState.value =
                    WelcomeUiState.Error(
                        AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                    )
            }
        }
    }

    sealed class WelcomeUiState {
        data object Empty : WelcomeUiState()
        data object SampleDataLoaded: WelcomeUiState()

        //data object Loading : WelcomeUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : WelcomeUiState()

    }
}