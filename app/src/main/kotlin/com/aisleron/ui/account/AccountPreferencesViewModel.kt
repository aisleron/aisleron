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
import com.aisleron.domain.preferences.SyncStatusPreference
import com.aisleron.domain.preferences.syncpreferences.usecase.GetSyncPreferencesFlowUseCase
import com.aisleron.domain.preferences.syncpreferences.usecase.GetSyncPreferencesUseCase
import com.aisleron.domain.preferences.syncpreferences.usecase.SetCustomSyncServiceDetailsUseCase
import com.aisleron.domain.preferences.syncpreferences.usecase.SetSyncOnMobileDataUseCase
import com.aisleron.domain.preferences.syncpreferences.usecase.SetSyncServiceUseCase
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.domain.sync.usecase.GetSessionStatusUseCase
import com.aisleron.domain.sync.usecase.RefreshSessionStatusUseCase
import com.aisleron.domain.sync.usecase.ScheduleForceSyncUseCase
import com.aisleron.domain.sync.usecase.SignOutUseCase
import com.aisleron.ui.base.UiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AccountPreferencesViewModel(
    private val signOutUseCase: SignOutUseCase,
    getSyncPreferencesUseCase: GetSyncPreferencesUseCase,
    getSyncPreferencesFlowUseCase: GetSyncPreferencesFlowUseCase,
    private val setCustomSyncServiceDetailsUseCase: SetCustomSyncServiceDetailsUseCase,
    getSessionStatusUseCase: GetSessionStatusUseCase,
    private val refreshSessionStatusUseCase: RefreshSessionStatusUseCase,
    private val setSyncOnMobileDataUseCase: SetSyncOnMobileDataUseCase,
    private val setSyncServiceUseCase: SetSyncServiceUseCase,
    private val scheduleForceSyncUseCase: ScheduleForceSyncUseCase,
    private val logger: Logger,
    debounceTime: Long = 300,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private val _isLoading = MutableStateFlow(false)
    private val _sessionStatusFlow = getSessionStatusUseCase()
        .transformLatest { status ->
            if (status is SyncSessionStatus.Loading) {
                delay(debounceTime.milliseconds)
            }
            emit(status)
        }

    private val _uiEvent = MutableStateFlow<UiEvent<UiEffect>?>(null)
    val uiEvent: StateFlow<UiEvent<UiEffect>?> = _uiEvent.asStateFlow()

    val uiState: StateFlow<AccountPreferencesUiState> = combine(
        getSyncPreferencesFlowUseCase(),
        _isLoading,
        _sessionStatusFlow
    ) { preferences, loading, status ->
        AccountPreferencesUiState(
            serviceUrl = preferences.serviceUrl,
            syncOnMobileData = preferences.syncOnMobileData,
            syncServicePreference = preferences.syncServicePreference,
            lastSyncDate = preferences.lastSyncedAt,
            lastSyncStatus = preferences.lastSyncStatus,
            lastFailedReason = preferences.lastFailedReason,
            isLoading = loading,
            sessionStatus = status
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = run {
            val initialPrefs = getSyncPreferencesUseCase()
            AccountPreferencesUiState(
                serviceUrl = initialPrefs.serviceUrl,
                syncOnMobileData = initialPrefs.syncOnMobileData,
                syncServicePreference = initialPrefs.syncServicePreference,
                lastSyncDate = initialPrefs.lastSyncedAt,
                lastSyncStatus = initialPrefs.lastSyncStatus,
                lastFailedReason = initialPrefs.lastFailedReason
            )
        }
    )

    fun signOut() {
        coroutineScope.launch {
            try {
                _isLoading.value = true
                signOutUseCase().onFailure { throwable ->
                    logger.e(TAG, throwable.message.orEmpty(), throwable)
                    _uiEvent.value =
                        UiEvent(UiEffect.SignOutFailure(throwable.exceptionCode))
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSyncServiceDetails(url: String, key: String) {
        setCustomSyncServiceDetailsUseCase(url, key)
        refreshSessionStatusUseCase()
    }

    fun setSyncOnMobileData(syncOnMobileData: Boolean) {
        setSyncOnMobileDataUseCase(syncOnMobileData)
    }

    fun setSyncService(syncServicePreference: SyncServicePreference) {
        setSyncServiceUseCase(syncServicePreference)
        refreshSessionStatusUseCase()
    }

    fun syncNow() {
        scheduleForceSyncUseCase()
        _uiEvent.value = UiEvent(UiEffect.SyncScheduled)
    }

    companion object {
        const val TAG = "AccountPreferencesViewModel"
    }

    sealed interface UiEffect {
        data object SyncScheduled : UiEffect
        data class SignOutFailure(val errorCode: AisleronException.ExceptionCode) :
            UiEffect
    }
}

data class AccountPreferencesUiState(
    val serviceUrl: String = "",
    val syncOnMobileData: Boolean = false,
    val syncServicePreference: SyncServicePreference = SyncServicePreference.NONE,
    val isLoading: Boolean = false,
    val sessionStatus: SyncSessionStatus = SyncSessionStatus.NotConfigured,
    val lastSyncDate: Long = 0,
    val lastSyncStatus: SyncStatusPreference = SyncStatusPreference.NONE,
    val lastFailedReason: String = ""
)