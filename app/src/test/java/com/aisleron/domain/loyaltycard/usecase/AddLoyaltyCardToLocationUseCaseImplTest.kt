package com.aisleron.domain.loyaltycard.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.GetLocationUseCase
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
    private lateinit var testData: TestDataManager
    private lateinit var loyaltyCardRepository: LoyaltyCardRepository
    private lateinit var addLoyaltyCardToLocationUseCase: AddLoyaltyCardToLocationUseCaseImpl

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        loyaltyCardRepository = testData.getRepository<LoyaltyCardRepository>()
        val getLocationUseCase = GetLocationUseCase(testData.getRepository<LocationRepository>())
        addLoyaltyCardToLocationUseCase =
            AddLoyaltyCardToLocationUseCaseImpl(loyaltyCardRepository, getLocationUseCase)
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
    fun invoke_ValidLocationAndCard_AddsLoyaltyCardToLocation() = runTest {
        val locationId = getLocation().id
        val loyaltyCardId = getLoyaltyCard().id

        val locationLoyaltyCardBefore = loyaltyCardRepository.getForLocation(locationId)

        addLoyaltyCardToLocationUseCase(locationId, loyaltyCardId)

        val locationLoyaltyCardAfter = loyaltyCardRepository.getForLocation(locationId)

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