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

package com.aisleron.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.Preference
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCase
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCase
import com.aisleron.domain.base.AisleronException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val backupDatabaseUseCase: BackupDatabaseUseCase,
    private val restoreDatabaseUseCase: RestoreDatabaseUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    private val preferenceHandlers =
        mutableMapOf<String, BackupRestoreDbPreferenceHandler>()
    private val _uiState = MutableStateFlow<UiState>(UiState.Empty)
    val uiState: StateFlow<UiState> = _uiState

    fun handleOnPreferenceClick(preferenceOption: SettingsFragment.PreferenceOption, uri: Uri) {
        val preferenceHandler = preferenceHandlers.getValue(preferenceOption.key)
        _uiState.value = UiState.Processing(preferenceHandler.getProcessingMessage())

        coroutineScope.launch {
            try {
                preferenceHandler.handleOnPreferenceClick(uri)
                _uiState.value = UiState.Success(preferenceHandler.getSuccessMessage())
            } catch (e: AisleronException) {
                _uiState.value = UiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _uiState.value =
                    UiState.Error(AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    fun setPreferenceValue(preferenceOption: SettingsFragment.PreferenceOption, value: String) {
        preferenceHandlers.getValue(preferenceOption.key).setValue(value)
    }

    fun getPreferenceValue(preferenceOption: SettingsFragment.PreferenceOption): String {
        return preferenceHandlers.getValue(preferenceOption.key).getValue()
    }

    fun preferenceHandlerFactory(
        preferenceOption: SettingsFragment.PreferenceOption, preference: Preference
    ): BackupRestoreDbPreferenceHandler {
        val result = when (preferenceOption) {
            SettingsFragment.PreferenceOption.BACKUP_FOLDER ->
                BackupFolderPreferenceHandler(preference)

            SettingsFragment.PreferenceOption.BACKUP_DATABASE ->
                BackupDbPreferenceHandler(preference, backupDatabaseUseCase)

            SettingsFragment.PreferenceOption.RESTORE_DATABASE ->
                RestoreDbPreferenceHandler(preference, restoreDatabaseUseCase)
        }

        preferenceHandlers[preferenceOption.key] = result

        return result
    }

    sealed class UiState {
        data object Empty : UiState()
        data class Processing(val message: String?) : UiState()
        data class Success(val message: String) : UiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode,
            val errorMessage: String?
        ) : UiState()
    }
}