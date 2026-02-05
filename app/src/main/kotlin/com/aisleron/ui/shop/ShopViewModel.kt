/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationMaxRankUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardToLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCase
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.RemoveLoyaltyCardFromLocationUseCase
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCase
import com.aisleron.domain.note.usecase.GetNoteParentUseCase
import com.aisleron.ui.note.NoteParentRef
import com.aisleron.ui.note.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(
    private val addLocationUseCase: AddLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val addLoyaltyCardUseCase: AddLoyaltyCardUseCase,
    private val addLoyaltyCardToLocationUseCase: AddLoyaltyCardToLocationUseCase,
    private val removeLoyaltyCardFromLocationUseCase: RemoveLoyaltyCardFromLocationUseCase,
    private val getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase,
    private val getNoteParentUseCase: GetNoteParentUseCase,
    private val applyNoteChangesUseCase: ApplyNoteChangesUseCase,
    private val getLocationMaxRankUseCase: GetLocationMaxRankUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel(), NoteViewModel {
    private var _location: Location? = null
    private var _initialLoyaltyCardId: Int? = null
    private var _loyaltyCard: LoyaltyCard? = null
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    private val _uiData = MutableStateFlow(ShopUiData())
    val uiData: StateFlow<ShopUiData> = _uiData

    private val _shopUiState = MutableStateFlow<ShopUiState>(ShopUiState.Empty)
    val shopUiState: StateFlow<ShopUiState> = _shopUiState

    override val noteFlow: StateFlow<String>
        get() = _uiData.map { it.noteText }.stateIn(
            coroutineScope,
            SharingStarted.Lazily,
            _uiData.value.noteText
        )

    private var hydrated = false

    fun hydrate(locationId: Int) {
        if (hydrated) return
        hydrated = true

        coroutineScope.launch {
            _shopUiState.value = ShopUiState.Loading
            _location = getLocation(locationId)
            _loyaltyCard = getLoyaltyCardForLocationUseCase(locationId)
            _initialLoyaltyCardId = _loyaltyCard?.id
            _uiData.value = ShopUiData(
                locationName = _location?.name.orEmpty(),
                pinned = _location?.pinned == true,
                showDefaultAisle = _location?.showDefaultAisle != false,
                loyaltyCardName = _loyaltyCard?.name.orEmpty(),
                noteText = _location?.note?.noteText.orEmpty()
            )

            _shopUiState.value = ShopUiState.Empty
        }
    }

    private suspend fun getLocation(locationId: Int): Location? {
        return getNoteParentUseCase(NoteParentRef.Location(locationId)) as Location?
    }

    fun saveLocation() {
        coroutineScope.launch {
            val name = _uiData.value.locationName
            val pinned = _uiData.value.pinned
            val showDefaultAisle = _uiData.value.showDefaultAisle
            val noteText = _uiData.value.noteText
            if (name.isBlank()) return@launch

            _shopUiState.value = ShopUiState.Loading
            try {
                _location?.let {
                    //Save the loyalty card details first, or it won't update correctly on the
                    //shopping list page due to the Location flow collection
                    saveLoyaltyCard(it.id, _initialLoyaltyCardId, _loyaltyCard)
                    updateLocation(it, name, pinned, showDefaultAisle)
                } ?: run {
                    val locationId = addLocation(name, pinned, showDefaultAisle)
                    saveLoyaltyCard(locationId, _initialLoyaltyCardId, _loyaltyCard)

                    _location = getLocation(locationId)
                }

                _location?.let {
                    val note = Note(it.noteId ?: 0, noteText)
                    applyNoteChangesUseCase(it, note)
                }

                _shopUiState.value = ShopUiState.Success
            } catch (e: AisleronException) {
                _shopUiState.value = ShopUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _shopUiState.value =
                    ShopUiState.Error(AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    private suspend fun saveLoyaltyCard(
        locationId: Int, initialLoyaltyCardId: Int?, loyaltyCard: LoyaltyCard?
    ) {
        //Remove the existing loyalty card, if it exists
        initialLoyaltyCardId?.let { removeLoyaltyCardFromLocationUseCase(locationId, it) }
        loyaltyCard?.let {
            //Add loyalty card to database
            val cardId = addLoyaltyCardUseCase(it)

            //Add loyalty card to location
            addLoyaltyCardToLocationUseCase(locationId, cardId)
        }
    }

    fun updateLocationName(name: String) {
        if (uiData.value.locationName != name) {
            _uiData.value = _uiData.value.copy(locationName = name)
        }
    }

    fun updatePinned(pinned: Boolean) {
        if (uiData.value.pinned != pinned) {
            _uiData.value = _uiData.value.copy(pinned = pinned)
        }
    }

    fun updateShowDefaultAisle(showDefaultAisle: Boolean) {
        if (uiData.value.showDefaultAisle != showDefaultAisle) {
            _uiData.value = _uiData.value.copy(showDefaultAisle = showDefaultAisle)
        }
    }

    private fun updateLoyaltyCard(loyaltyCard: LoyaltyCard?) {
        _loyaltyCard = loyaltyCard
        _uiData.value = _uiData.value.copy(loyaltyCardName = loyaltyCard?.name.orEmpty())
    }

    fun setLoyaltyCard(loyaltyCard: LoyaltyCard?) {
        updateLoyaltyCard(loyaltyCard)
    }

    fun removeLoyaltyCard() {
        updateLoyaltyCard(null)
    }

    private suspend fun updateLocation(
        location: Location, name: String, pinned: Boolean, showDefaultAisle: Boolean
    ): Int {
        val updateLocation =
            location.copy(name = name, pinned = pinned, showDefaultAisle = showDefaultAisle)
        updateLocationUseCase(updateLocation)
        return updateLocation.id
    }

    private suspend fun addLocation(name: String, pinned: Boolean, showDefaultAisle: Boolean): Int {
        return addLocationUseCase(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = name,
                pinned = pinned,
                aisles = emptyList(),
                showDefaultAisle = showDefaultAisle,
                expanded = true,
                rank = getLocationMaxRankUseCase() + 1
            )
        )
    }

    override fun updateNote(noteText: String) {
        _uiData.value = _uiData.value.copy(noteText = noteText)
    }

    sealed class ShopUiState {
        data object Empty : ShopUiState()
        data object Loading : ShopUiState()
        data object Success : ShopUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ShopUiState()
    }
}