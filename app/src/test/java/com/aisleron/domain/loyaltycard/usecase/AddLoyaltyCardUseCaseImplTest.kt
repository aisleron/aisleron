package com.aisleron.domain.loyaltycard.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class AddLoyaltyCardUseCaseImplTest {
    private lateinit var testData: TestDataManager
    private lateinit var addLoyaltyCardUseCase: AddLoyaltyCardUseCaseImpl
    private lateinit var loyaltyCardRepository: LoyaltyCardRepository
    private val loyaltyCardProvider = LoyaltyCardProviderType.CATIMA
    private val loyaltyCardName = "Test Card"
    private val loyaltyCardIntent = "Dummy Intent"

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        loyaltyCardRepository = testData.getRepository<LoyaltyCardRepository>()
        addLoyaltyCardUseCase =
            AddLoyaltyCardUseCaseImpl(testData.getRepository<LoyaltyCardRepository>())
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

        val countAfter = loyaltyCardRepository.getAll().count()
        val addedCard = loyaltyCardRepository.get(resultId)

        assertEquals(countBefore + 1, countAfter)
        assertNotNull(addedCard)
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