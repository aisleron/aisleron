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

package com.aisleron.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardToLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCase
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.RemoveLoyaltyCardFromLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopViewModel(
    private val addLocationUseCase: AddLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val addLoyaltyCardUseCase: AddLoyaltyCardUseCase,
    private val addLoyaltyCardToLocationUseCase: AddLoyaltyCardToLocationUseCase,
    private val removeLoyaltyCardFromLocationUseCase: RemoveLoyaltyCardFromLocationUseCase,
    private val getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private var _initialLoyaltyCardId: Int? = null

    private var _loyaltyCard: LoyaltyCard? = null
    val loyaltyCardName: String? get() = _loyaltyCard?.name

    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    val pinned: Boolean get() = _location?.pinned == true
    val locationName: String? get() = _location?.name
    val showDefaultAisle: Boolean get() = _location?.showDefaultAisle != false

    private var _location: Location? = null

    private val _shopUiState = MutableStateFlow<ShopUiState>(ShopUiState.Empty)
    val shopUiState: StateFlow<ShopUiState> = _shopUiState

    fun hydrate(locationId: Int) {
        coroutineScope.launch {
            _shopUiState.value = ShopUiState.Loading
            _location = getLocationUseCase(locationId)
            _loyaltyCard = getLoyaltyCardForLocationUseCase(locationId)
            _initialLoyaltyCardId = _loyaltyCard?.id
            _shopUiState.value = ShopUiState.Updated(this@ShopViewModel)
        }
    }

    fun saveLocation(name: String, pinned: Boolean, showDefaultAisle: Boolean) {
        coroutineScope.launch {
            _shopUiState.value = ShopUiState.Loading
            try {
                _location?.let {
                    //Save the loyalty card details first, or it won't update correctly on the
                    //shopping list page due to teh dat aflow collection
                    saveLoyaltyCard(it.id, _initialLoyaltyCardId, _loyaltyCard)
                    updateLocation(it, name, pinned, showDefaultAisle)
                } ?: run {
                    val locationId = addLocation(name, pinned, showDefaultAisle)
                    saveLoyaltyCard(locationId, _initialLoyaltyCardId, _loyaltyCard)
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

    fun setLoyaltyCard(loyaltyCard: LoyaltyCard?) {
        _loyaltyCard = loyaltyCard
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
                showDefaultAisle = showDefaultAisle
            )
        )
    }

    fun removeLoyaltyCard() {
        _loyaltyCard = null
    }

    sealed class ShopUiState {
        data object Empty : ShopUiState()
        data object Loading : ShopUiState()
        data object Success : ShopUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ShopUiState()

        data class Updated(val shop: ShopViewModel) : ShopUiState()
    }
}