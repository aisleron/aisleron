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

package com.aisleron.ui.aisle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.shoppinglist.AisleShoppingListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AisleViewModel(
    private val addAisleUseCase: AddAisleUseCase,
    private val updateAisleUseCase: UpdateAisleUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    private val _uiState = MutableStateFlow<AisleUiState>(AisleUiState.Empty)
    val uiState = _uiState.asStateFlow()

    fun addAisle(aisleName: String, locationId: Int) {
        coroutineScope.launch {
            try {
                if (aisleName.isNotBlank()) {
                    addAisleUseCase(
                        Aisle(
                            name = aisleName,
                            products = emptyList(),
                            locationId = locationId,
                            isDefault = false,
                            rank = 0,
                            id = 0,
                            expanded = true
                        )
                    )
                }
                _uiState.value = AisleUiState.Success
            } catch (e: AisleronException) {
                _uiState.value = AisleUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _uiState.value = AisleUiState.Error(
                    AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                )
            }
        }
    }

    fun updateAisleName(aisle: AisleShoppingListItem, newName: String) {
        coroutineScope.launch {
            try {
                if (newName.isNotBlank()) {
                    updateAisleUseCase(
                        Aisle(
                            name = newName,
                            products = emptyList(),
                            locationId = aisle.locationId,
                            isDefault = aisle.isDefault,
                            rank = aisle.rank,
                            id = aisle.id,
                            expanded = aisle.expanded
                        )
                    )
                }

                _uiState.value = AisleUiState.Success
            } catch (e: AisleronException) {
                _uiState.value = AisleUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _uiState.value = AisleUiState.Error(
                    AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                )
            }
        }
    }

    fun clearState() {
        _uiState.value = AisleUiState.Empty
    }

    sealed class AisleUiState {
        data object Empty : AisleUiState()
        data object Success : AisleUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode,
            val errorMessage: String?
        ) : AisleUiState()
    }
}
