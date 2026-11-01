/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.base.exceptionCode
import com.aisleron.domain.log.Logger
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.syncpreferences.usecase.GetSyncPreferencesUseCase
import com.aisleron.domain.sync.usecase.SignInWithEmailUseCase
import com.aisleron.ui.base.UiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val getSyncPreferencesUseCase: GetSyncPreferencesUseCase,
    private val logger: Logger,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    val syncServiceUrl: String get() = getSyncPreferencesUseCase().serviceUrl
    val syncServicePreference: SyncServicePreference get() = getSyncPreferencesUseCase().syncServicePreference

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _signInEvent = MutableStateFlow<UiEvent<UiEffect>?>(null)
    val signInEvent: StateFlow<UiEvent<UiEffect>?> = _signInEvent.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        coroutineScope.launch {
            try {
                _isLoading.value = true
                signInWithEmailUseCase(email, password)
                    .onSuccess {
                        _signInEvent.value = UiEvent(UiEffect.SignInSuccess)
                    }.onFailure { throwable ->
                        logger.e(TAG, throwable.message.orEmpty(), throwable)
                        _signInEvent.value = UiEvent(
                            UiEffect.SignInFailure(throwable.exceptionCode)
                        )
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        const val TAG = "SignInViewModel"
    }

    sealed interface UiEffect {
        data object SignInSuccess : UiEffect
        data class SignInFailure(val errorCode: AisleronException.ExceptionCode) : UiEffect
    }
}