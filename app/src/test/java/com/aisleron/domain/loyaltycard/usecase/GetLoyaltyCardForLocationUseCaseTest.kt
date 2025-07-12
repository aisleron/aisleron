package com.aisleron.domain.loyaltycard.usecase

import com.aisleron.data.TestDataManager
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
    private lateinit var testData: TestDataManager
    private lateinit var loyaltyCardRepository: LoyaltyCardRepository
    private lateinit var getLoyaltyCardForLocationUseCase: GetLoyaltyCardForLocationUseCaseImpl

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        loyaltyCardRepository = testData.getRepository<LoyaltyCardRepository>()
        getLoyaltyCardForLocationUseCase =
            GetLoyaltyCardForLocationUseCaseImpl(loyaltyCardRepository)
    }

    private suspend fun getLocation(): Location {
        return testData.getRepository<LocationRepository>().getShops().first().first()
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