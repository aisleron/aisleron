package com.aisleron.domain.loyaltycard.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.testdata.data.loyaltycard.LocationLoyaltyCardDaoTestImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class RemoveLoyaltyCardFromLocationUseCaseImplTest {
    private lateinit var testData: TestDataManager
    private lateinit var loyaltyCardRepository: LoyaltyCardRepository
    private lateinit var removeLoyaltyCardFromLocationUseCase: RemoveLoyaltyCardFromLocationUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        loyaltyCardRepository = testData.getRepository<LoyaltyCardRepository>()
        removeLoyaltyCardFromLocationUseCase =
            RemoveLoyaltyCardFromLocationUseCaseImpl(loyaltyCardRepository)
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
    fun invoke_AssignedCard_CardRemoved() = runTest {
        val location = getLocation()
        val removeCardId = getLoyaltyCard().id
        loyaltyCardRepository.addToLocation(location.id, removeCardId)

        val cardBefore = loyaltyCardRepository.getForLocation(location.id)

        removeLoyaltyCardFromLocationUseCase(location.id, removeCardId)

        val cardAfter = loyaltyCardRepository.getForLocation(location.id)

        assertNotNull(cardBefore)
        assertNull(cardAfter)
    }

    @Test
    fun invoke_UnrelatedCardUnchanged_OtherCardsRemain() = runTest {
        val location1 = 1
        val location2 = 2
        val locationLoyaltyCardDao =
            (testData.locationLoyaltyCardDao() as LocationLoyaltyCardDaoTestImpl)

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

        val countBefore = locationLoyaltyCardDao.getAll().count()

        removeLoyaltyCardFromLocationUseCase(location1, removeCardId)

        val countAfter = locationLoyaltyCardDao.getAll().count()
        val otherCard = loyaltyCardRepository.getForLocation(location2)

        assertEquals(countBefore - 1, countAfter)
        assertNotNull(otherCard)
    }


    @Test
    fun invoke_InvalidLocation_NoCardRemoved() = runTest {
        val locationLoyaltyCardDao =
            (testData.locationLoyaltyCardDao() as LocationLoyaltyCardDaoTestImpl)

        val location = getLocation()
        val loyaltyCard = getLoyaltyCard()
        loyaltyCardRepository.addToLocation(location.id, loyaltyCard.id)

        val countBefore = locationLoyaltyCardDao.getAll().count()

        removeLoyaltyCardFromLocationUseCase(999, loyaltyCard.id)

        val countAfter = locationLoyaltyCardDao.getAll().count()

        assertEquals(countBefore, countAfter)
    }

    @Test
    fun invoke_InvalidCard_NoCardRemoved() = runTest {
        val locationLoyaltyCardDao =
            (testData.locationLoyaltyCardDao() as LocationLoyaltyCardDaoTestImpl)

        val location = getLocation()
        val loyaltyCard = getLoyaltyCard()
        loyaltyCardRepository.addToLocation(location.id, loyaltyCard.id)

        val countBefore = locationLoyaltyCardDao.getAll().count()

        removeLoyaltyCardFromLocationUseCase(location.id, 999)

        val countAfter = locationLoyaltyCardDao.getAll().count()

        assertEquals(countBefore, countAfter)
    }
}

/**
 * Test given card removed from location
 * Test no other cards removed
 * Test no cards removed when invalid location
 * Test no cards removed when invalid loyalty card
 */