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

package com.aisleron.domain.loyaltycard.usecase

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository

interface AddLoyaltyCardToLocationUseCase {
    suspend operator fun invoke(locationId: Int, loyaltyCardId: Int)
}

class AddLoyaltyCardToLocationUseCaseImpl(
    private val loyaltyCardRepository: LoyaltyCardRepository,
    private val getLocationUseCase: GetLocationUseCase
) : AddLoyaltyCardToLocationUseCase {
    override suspend operator fun invoke(locationId: Int, loyaltyCardId: Int) {
        getLocationUseCase(locationId)
            ?: throw AisleronException.InvalidLocationException("Invalid Location Id provided")

        loyaltyCardRepository.get(loyaltyCardId)
            ?: throw AisleronException.InvalidLoyaltyCardException("Invalid Loyalty Card Id provided")

        loyaltyCardRepository.addToLocation(locationId, loyaltyCardId)
    }
}

