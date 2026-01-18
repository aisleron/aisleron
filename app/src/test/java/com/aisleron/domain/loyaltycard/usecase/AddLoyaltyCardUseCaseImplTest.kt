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
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class AddLoyaltyCardUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var addLoyaltyCardUseCase: AddLoyaltyCardUseCase
    private lateinit var loyaltyCardRepository: LoyaltyCardRepository
    private val loyaltyCardProvider = LoyaltyCardProviderType.CATIMA
    private val loyaltyCardName = "Test Card"
    private val loyaltyCardIntent = "Dummy Intent"

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        loyaltyCardRepository = dm.getRepository<LoyaltyCardRepository>()
        addLoyaltyCardUseCase = dm.getUseCase()
    }

    @Test
    fun invoke_NoExistingCard_AddsCardAndReturnsNewId() = runTest {
        val newCard = LoyaltyCard(
            id = 0,
            provider = loyaltyCardProvider,
            intent = loyaltyCardIntent,
            name = loyaltyCardName
        )

        val countBefore = loyaltyCardRepository.getAll().count()

        val resultId = addLoyaltyCardUseCase(newCard)

        val addedCard = loyaltyCardRepository.get(resultId)
        assertNotNull(addedCard)

        val countAfter = loyaltyCardRepository.getAll().count()
        assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun invoke_ExistingCardFound_UpdatesCardAndReturnsExistingId() = runTest {
        val updatedName = "$loyaltyCardName Updated"
        val cardId = loyaltyCardRepository.add(
            LoyaltyCard(
                id = 0,
                provider = loyaltyCardProvider,
                intent = loyaltyCardIntent,
                name = loyaltyCardName
            )
        )

        val existingCard = loyaltyCardRepository.get(cardId)!!

        val resultId = addLoyaltyCardUseCase(existingCard.copy(name = updatedName))

        val updatedCard = loyaltyCardRepository.get(resultId)
        assertEquals(cardId, resultId)
        assertEquals(existingCard.copy(name = updatedName), updatedCard)
    }
}