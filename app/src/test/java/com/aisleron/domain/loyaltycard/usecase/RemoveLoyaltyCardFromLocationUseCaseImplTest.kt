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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class RemoveLoyaltyCardFromLocationUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var loyaltyCardRepository: LoyaltyCardRepository
    private lateinit var removeLoyaltyCardFromLocationUseCase: RemoveLoyaltyCardFromLocationUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        loyaltyCardRepository = dm.getRepository<LoyaltyCardRepository>()
        removeLoyaltyCardFromLocationUseCase = dm.getUseCase()
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
    fun invoke_AssignedCard_CardRemoved() = runTest {
        val location = getLocation()
        val removeCardId = getLoyaltyCard().id
        loyaltyCardRepository.addToLocation(location.id, removeCardId)
        val cardBefore = loyaltyCardRepository.getForLocation(location.id)

        removeLoyaltyCardFromLocationUseCase(location.id, removeCardId)

        assertNotNull(cardBefore)

        val cardAfter = loyaltyCardRepository.getForLocation(location.id)
        assertNull(cardAfter)
    }

    @Test
    fun invoke_UnrelatedCardUnchanged_OtherCardsRemain() = runTest {
        val location1 = 1
        val location2 = 2
        val otherCardId = getLoyaltyCard().id
        val removeCardId = loyaltyCardRepository.add(
            LoyaltyCard(
                id = 0,
                provider = LoyaltyCardProviderType.CATIMA,
                name = "Remove Card",
                intent = "Dummy Intent"
            )
        )

        loyaltyCardRepository.addToLocation(location1, removeCardId)
        loyaltyCardRepository.addToLocation(location2, otherCardId)

        removeLoyaltyCardFromLocationUseCase(location1, removeCardId)

        val otherCard = loyaltyCardRepository.getForLocation(location2)
        assertNotNull(otherCard)
    }


    @Test
    fun invoke_InvalidLocation_NoCardRemoved() = runTest {
        val location = getLocation()
        val loyaltyCard = getLoyaltyCard()
        loyaltyCardRepository.addToLocation(location.id, loyaltyCard.id)
        val countBefore = loyaltyCardRepository.getAll().count()

        removeLoyaltyCardFromLocationUseCase(999, loyaltyCard.id)

        val countAfter = loyaltyCardRepository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun invoke_InvalidCard_NoCardRemoved() = runTest {
        val location = getLocation()
        val loyaltyCard = getLoyaltyCard()
        loyaltyCardRepository.addToLocation(location.id, loyaltyCard.id)

        removeLoyaltyCardFromLocationUseCase(location.id, 999)

        val locationLoyaltyCardAfter = loyaltyCardRepository.getForLocation(location.id)
        assertNotNull(locationLoyaltyCardAfter)
    }
}