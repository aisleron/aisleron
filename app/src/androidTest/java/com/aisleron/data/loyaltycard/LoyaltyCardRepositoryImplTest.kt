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

package com.aisleron.data.loyaltycard

import com.aisleron.data.RepositoryImplTest
import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LoyaltyCardRepositoryImplTest : RepositoryImplTest<LoyaltyCard>() {
    private val loyaltyCardRepository: LoyaltyCardRepository get() = repository as LoyaltyCardRepository

    override fun initRepository(): BaseRepository<LoyaltyCard> =
        LoyaltyCardRepositoryImpl(
            loyaltyCardDao = get<LoyaltyCardDao>(),
            locationLoyaltyCardDao = get<LocationLoyaltyCardDao>(),
            loyaltyCardMapper = LoyaltyCardMapper()
        )

    override suspend fun getSingleNewItem(): LoyaltyCard =
        LoyaltyCard(
            id = 0,
            name = "Card 1",
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Dummy Intent"
        )

    override suspend fun getMultipleNewItems(): List<LoyaltyCard> {
        return listOf(
            getSingleNewItem(),
            getSingleNewItem().copy(name = "Card 2")
        )
    }

    override suspend fun getInvalidItem(): LoyaltyCard =
        getSingleNewItem().copy(id = -1)

    override fun getUpdatedItem(item: LoyaltyCard): LoyaltyCard =
        item.copy(name = "${item.name} Updated")

    @Test
    override fun add_SingleItemProvided_AddItem() {
        super.add_SingleItemProvided_AddItem()
    }

    @Test
    fun getProviderCard_ValidProviderProvided_ReturnCard() = runTest {
        val card = repository.get(addSingleItem())!!

        val result = loyaltyCardRepository.getProviderCard(card.provider, card.intent)

        assertEquals(card, result)
    }

    @Test
    fun getProviderCard_InvalidProviderProvided_ReturnNull() = runTest {
        val result = loyaltyCardRepository.getProviderCard(LoyaltyCardProviderType.CATIMA, "")

        assertNull(result)
    }

    private suspend fun getLocation(): Location =
        get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

    @Test
    fun addToLocation_ValidLocationAndCard_CardAddedToLocation() = runTest {
        val cardId = addSingleItem()
        val location = getLocation()

        loyaltyCardRepository.addToLocation(location.id, cardId)

        val result = loyaltyCardRepository.getForLocation(location.id)
        assertNotNull(result)
        assertEquals(cardId, result.id)
    }

    @Test
    fun removeFromLocation_ValidLocationAndCard_CardRemovedFromLocation() = runTest {
        val cardId = addSingleItem()
        val location = getLocation()
        loyaltyCardRepository.addToLocation(location.id, cardId)

        loyaltyCardRepository.removeFromLocation(location.id, cardId)

        val result = loyaltyCardRepository.getForLocation(location.id)
        assertNull(result)
    }
}