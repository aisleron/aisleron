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
        data object SampleDataLoaded : WelcomeUiState()

        //data object Loading : WelcomeUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : WelcomeUiState()

    }
}