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
import com.aisleron.domain.base.AisleronException
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
import org.junit.jupiter.api.assertThrows

class AddLoyaltyCardToLocationUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var addLoyaltyCardToLocationUseCase: AddLoyaltyCardToLocationUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        addLoyaltyCardToLocationUseCase = dm.getUseCase()
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

        val loyaltyCardId = dm.getRepository<LoyaltyCardRepository>().add(loyaltyCard)

        return loyaltyCard.copy(id = loyaltyCardId)
    }

    @Test
    fun invoke_ValidLocationAndCard_AddsLoyaltyCardToLocation() = runTest {
        val locationId = getLocation().id
        val loyaltyCardId = getLoyaltyCard().id
        val locationLoyaltyCardBefore =
            dm.getRepository<LoyaltyCardRepository>().getForLocation(locationId)

        addLoyaltyCardToLocationUseCase(locationId, loyaltyCardId)

        val locationLoyaltyCardAfter =
            dm.getRepository<LoyaltyCardRepository>().getForLocation(locationId)
        assertNull(locationLoyaltyCardBefore)
        assertEquals(loyaltyCardId, locationLoyaltyCardAfter?.id)

    }

    @Test
    fun invoke_InvalidLocation_ThrowsInvalidLocationException() = runTest {
        val locationId = 999
        val loyaltyCardId = getLoyaltyCard().id

        assertThrows<AisleronException.InvalidLocationException> {
            addLoyaltyCardToLocationUseCase(locationId, loyaltyCardId)
        }
    }

    @Test
    fun invoke_InvalidLoyaltyCard_ThrowsInvalidLoyaltyCardException() = runTest {
        val locationId = getLocation().id
        val loyaltyCardId = 999

        assertThrows<AisleronException.InvalidLoyaltyCardException> {
            addLoyaltyCardToLocationUseCase(locationId, loyaltyCardId)
        }
    }
}

/**
 * Test removed on location delete - this should be in remove location tests
 * Test removed on loyalty card delete
 */