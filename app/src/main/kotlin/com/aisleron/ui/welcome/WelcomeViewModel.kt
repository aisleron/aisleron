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
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val createSampleDataUseCase: CreateSampleDataUseCase,
    private val getAllProductsUseCase: GetAllProductsUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val _productsLoaded = MutableStateFlow(false)
    val productsLoaded = _productsLoaded.asStateFlow()

    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private val _welcomeUiState = MutableStateFlow<WelcomeUiState>(
        WelcomeUiState.Empty
    )

    val welcomeUiState = _welcomeUiState.asStateFlow()

    fun createSampleData() {
        coroutineScope.launch {
            try {
                _productsLoaded.value = true
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

    fun checkForProducts() {
        coroutineScope.launch {
            try {
                val hasProducts = getAllProductsUseCase().any()
                _productsLoaded.value = hasProducts
            } catch (e: AisleronException) {
                _welcomeUiState.value = WelcomeUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _welcomeUiState.value = WelcomeUiState.Error(
                    AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                )
            }
        }
    }

    fun clearState() {
        _welcomeUiState.value = WelcomeUiState.Empty
    }

    sealed class WelcomeUiState {
        data object Empty : WelcomeUiState()
        data object SampleDataLoaded : WelcomeUiState()

        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : WelcomeUiState()

    }
}