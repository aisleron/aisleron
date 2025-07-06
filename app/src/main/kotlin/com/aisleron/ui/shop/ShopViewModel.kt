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
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopViewModel(
    private val addLocationUseCase: AddLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val addLoyaltyCardUseCase: AddLoyaltyCardUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private var _loyaltyCard: LoyaltyCard? = null
    val loyaltyCard: LoyaltyCard? get() = _loyaltyCard

    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    val pinned: Boolean get() = location?.pinned == true
    val defaultFilter: FilterType? get() = location?.defaultFilter
    val type: LocationType? get() = location?.type
    val locationName: String? get() = location?.name
    val showDefaultAisle: Boolean get() = location?.showDefaultAisle != false

    private var location: Location? = null

    private val _shopUiState = MutableStateFlow<ShopUiState>(ShopUiState.Empty)
    val shopUiState: StateFlow<ShopUiState> = _shopUiState

    fun hydrate(locationId: Int) {
        coroutineScope.launch {
            _shopUiState.value = ShopUiState.Loading
            location = getLocationUseCase(locationId)
            _shopUiState.value = ShopUiState.Updated(this@ShopViewModel)
        }
    }

    fun saveLocation(name: String, pinned: Boolean, showDefaultAisle: Boolean) {
        coroutineScope.launch {
            _shopUiState.value = ShopUiState.Loading
            try {
                location?.let { updateLocation(it, name, pinned, showDefaultAisle) }
                    ?: addLocation(name, pinned, showDefaultAisle)

                _shopUiState.value = ShopUiState.Success
            } catch (e: AisleronException) {
                _shopUiState.value = ShopUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _shopUiState.value =
                    ShopUiState.Error(AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    fun addLoyaltyCard(loyaltyCard: LoyaltyCard?) {
        coroutineScope.launch {
            _loyaltyCard = loyaltyCard?.let {
                it.copy(id = addLoyaltyCardUseCase(it))
            }
        }
    }

    private suspend fun updateLocation(
        location: Location, name: String, pinned: Boolean, showDefaultAisle: Boolean
    ) {
        val updateLocation =
            location.copy(name = name, pinned = pinned, showDefaultAisle = showDefaultAisle)
        updateLocationUseCase(updateLocation)
        hydrate(updateLocation.id)
    }

    private suspend fun addLocation(name: String, pinned: Boolean, showDefaultAisle: Boolean) {
        val id = addLocationUseCase(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = name,
                pinned = pinned,
                aisles = emptyList(),
                showDefaultAisle = showDefaultAisle
                //TODO: Assign loyalty card here
            )
        )
        hydrate(id)
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