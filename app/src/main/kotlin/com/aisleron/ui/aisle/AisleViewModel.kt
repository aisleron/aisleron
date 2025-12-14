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
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.base.AisleronException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AisleViewModel(
    private val addAisleUseCase: AddAisleUseCase,
    private val updateAisleUseCase: UpdateAisleUseCase,
    private val getAisleUseCase: GetAisleUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    private var _aisleName = MutableStateFlow("")
    val aisleName: StateFlow<String> = _aisleName

    private var _aisleId: Int? = null
    val aisleId: Int get() = _aisleId!!

    private var _locationId: Int = -1

    private val _uiState = MutableStateFlow<AisleUiState>(AisleUiState.Empty)
    val uiState = _uiState.asStateFlow()

    fun hydrate(aisleId: Int, locationId: Int) {
        if (aisleId == _aisleId) return

        coroutineScope.launch {
            _aisleId = aisleId
            val aisle = getAisleUseCase(aisleId)
            aisle?.let {
                _aisleName.value = it.name
                _locationId = it.locationId
            } ?: run {
                _aisleName.value = ""
                _locationId = locationId
            }
        }
    }

    fun setAisleName(name: String) {
        _aisleName.value = name
    }

    fun addAisle() {
        val aisleName = aisleName.value
        coroutineScope.launch {
            try {
                if (aisleName.isNotBlank()) {
                    _aisleId = addAisleUseCase(
                        Aisle(
                            name = aisleName,
                            products = emptyList(),
                            locationId = _locationId,
                            isDefault = false,
                            rank = 0,
                            id = 0,
                            expanded = true
                        )
                    )
                }

                _uiState.value = AisleUiState.Success(_aisleId ?: -1)
            } catch (e: AisleronException) {
                _uiState.value = AisleUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _uiState.value = AisleUiState.Error(
                    AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                )
            }
        }
    }

    fun updateAisleName() {
        coroutineScope.launch {
            try {
                val newName = aisleName.value
                if (newName.isNotBlank()) {
                    val aisle = getAisleUseCase(aisleId)
                    aisle?.let {
                        updateAisleUseCase(it.copy(name = newName))
                    }
                }

                _uiState.value = AisleUiState.Success(aisleId)
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
        data class Success(val aisleId: Int) : AisleUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode,
            val errorMessage: String?
        ) : AisleUiState()
    }
}
