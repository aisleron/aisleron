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

package com.aisleron.domain.loyaltycard.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class GetLoyaltyCardForLocationUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var loyaltyCardRepository: LoyaltyCardRepository
    private lateinit var getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        loyaltyCardRepository = dm.getRepository<LoyaltyCardRepository>()
        getLoyaltyCardForLocationUseCase = dm.getUseCase()
    }

    private suspend fun getLocation(): Location {
        return dm.getRepository<LocationRepository>().getShops().first().first()
    }

    private suspend fun getLoyaltyCard(): LoyaltyCard {
        val loyaltyCard = LoyaltyCard(
            id = 0,
            name = "Loyalty Card Test",
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Dummy Intent"
        )

        val loyaltyCardId = loyaltyCardRepository.add(loyaltyCard)

        return loyaltyCard.copy(id = loyaltyCardId)
    }

    @Test
    fun invoke_LocationHasCard_ReturnsLoyaltyCard() = runTest {
        val locationId = getLocation().id
        val card = getLoyaltyCard()
        loyaltyCardRepository.addToLocation(locationId, card.id)

        val result = getLoyaltyCardForLocationUseCase(locationId)

        assertEquals(card, result)
    }

    @Test
    fun invoke_LocationHasNoCard_ReturnsNull() = runTest {
        val locationId = getLocation().id
        val result = getLoyaltyCardForLocationUseCase(locationId)
        assertNull(result)
    }

    @Test
    fun invoke_InvalidLocationId_ReturnsNull() = runTest {
        val invalidLocationId = -1
        val result = getLoyaltyCardForLocationUseCase(invalidLocationId)
        assertNull(result)
    }
}