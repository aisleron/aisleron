package com.aisleron.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.base.AisleronException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(coroutineScopeProvider: CoroutineScope? = null) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    private val preferenceHandlers =
        mutableMapOf<String, BackupRestoreDbPreferenceHandler>()
    private val _uiState = MutableStateFlow<UiState>(UiState.Empty)
    val uiState: StateFlow<UiState> = _uiState

    fun addPreferenceHandler(
        preferenceKey: String,
        backupRestoreDbPreferenceHandler: BackupRestoreDbPreferenceHandler
    ) {
        preferenceHandlers[preferenceKey] = backupRestoreDbPreferenceHandler
    }

    fun handleOnPreferenceClick(preferenceKey: String, uri: Uri) {
        preferenceHandlers[preferenceKey]?.let {
            _uiState.value = UiState.Processing(it.getProcessingMessage())

            coroutineScope.launch {
                try {
                    it.handleOnPreferenceClick(uri)
                    _uiState.value = UiState.Success(it.getSuccessMessage())
                } catch (e: AisleronException) {
                    _uiState.value = UiState.Error(e.exceptionCode, e.message)
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(AisleronException.GENERIC_EXCEPTION, e.message)
                }

                _uiState.value = UiState.Empty
            }
        }
    }

    fun getPreferenceHandler(preferenceKey: String) =
        preferenceHandlers[preferenceKey]

    sealed class UiState {
        data object Empty : UiState()
        data class Processing(val message: String?) : UiState()
        data class Success(val message: String?) : UiState()
        data class Error(val errorCode: String, val errorMessage: String?) : UiState()
    }
}