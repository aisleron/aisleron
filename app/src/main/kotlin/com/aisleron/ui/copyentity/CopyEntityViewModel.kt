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

package com.aisleron.ui.copyentity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.CopyLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.CopyProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CopyEntityViewModel(
    private val copyLocationUseCase: CopyLocationUseCase,
    private val copyProductUseCase: CopyProductUseCase,
    private val getProductUseCase: GetProductUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    private val _uiState = MutableStateFlow<CopyUiState>(CopyUiState.Idle)
    val uiState: StateFlow<CopyUiState> = _uiState

    fun copyEntity(type: CopyEntityType, newName: String) {
        coroutineScope.launch {
            _uiState.value = CopyUiState.Loading
            try {
                val id = when (type) {
                    is CopyEntityType.Location -> copyLocationUseCase(
                        getLocation(type.sourceId), newName
                    )

                    is CopyEntityType.Product -> copyProductUseCase(
                        getProduct(type.sourceId), newName
                    )
                }
                _uiState.value = CopyUiState.Success(id)
            } catch (e: AisleronException) {
                _uiState.value = CopyUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _uiState.value =
                    CopyUiState.Error(
                        AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                    )
            }
        }
    }

    private suspend fun getProduct(id: Int): Product =
        getProductUseCase(id)
            ?: throw AisleronException.InvalidProductException("Invalid Product Id provided")

    private suspend fun getLocation(id: Int): Location =
        getLocationUseCase(id)
            ?: throw AisleronException.InvalidLocationException("Invalid Location Id provided")

    sealed class CopyUiState {
        data object Idle : CopyUiState()
        data object Loading : CopyUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : CopyUiState()

        data class Success(val newId: Int) : CopyUiState()
    }
}