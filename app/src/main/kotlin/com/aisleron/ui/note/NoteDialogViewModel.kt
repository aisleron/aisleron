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

package com.aisleron.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteParent
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCase
import com.aisleron.domain.note.usecase.GetNoteParentUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDialogViewModel(
    private val getNoteParentUseCase: GetNoteParentUseCase,
    private val applyNoteChangesUseCase: ApplyNoteChangesUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private var _noteParent: NoteParent? = null
    val parentName: String get() = _noteParent?.name.orEmpty()

    private var _noteText: String = ""
    val noteText: String get() = _noteText

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun updateNote(noteText: String) {
        _noteText = noteText
    }

    fun saveNote() {
        _noteParent?.let {
            coroutineScope.launch {
                _uiState.value = UiState.Loading
                try {
                    val note = it.note?.copy(noteText = _noteText) ?: Note(0, _noteText)
                    applyNoteChangesUseCase(it, note)
                    _uiState.value = UiState.Success
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(
                        AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                    )
                }
            }
        }
    }

    suspend fun hydrate(noteParentRef: NoteParentRef) {
        if (_noteParent == null) {
            _noteParent = getNoteParentUseCase(noteParentRef)
            _noteText = _noteParent?.note?.noteText.orEmpty()
        }
    }

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data object Success : UiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode,
            val errorMessage: String?
        ) : UiState()
    }
}