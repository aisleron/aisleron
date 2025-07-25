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

package com.aisleron.ui.shop

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardToLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.AddLoyaltyCardUseCase
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.loyaltycard.usecase.RemoveLoyaltyCardFromLocationUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare

@RunWith(value = Parameterized::class)
class ShopViewModelTest(private val pinned: Boolean, private val showDefaultAisle: Boolean) :
    KoinTest {
    private lateinit var shopViewModel: ShopViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        shopViewModel = get<ShopViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun testSaveLocation_LocationExists_UpdateLocation() = runTest {
        val updatedLocationName = "Updated Location Name"
        val locationRepository = get<LocationRepository>()
        val existingLocation: Location = locationRepository.getAll().first()
        val countBefore: Int = locationRepository.getAll().count()

        shopViewModel.hydrate(existingLocation.id)
        shopViewModel.saveLocation(updatedLocationName, pinned, showDefaultAisle)

        val updatedLocation = locationRepository.get(existingLocation.id)
        val countAfter: Int = locationRepository.getAll().count()

        Assert.assertNotNull(updatedLocation)
        Assert.assertEquals(updatedLocationName, updatedLocation?.name)
        Assert.assertEquals(pinned, updatedLocation?.pinned)
        Assert.assertEquals(showDefaultAisle, updatedLocation?.showDefaultAisle)
        Assert.assertEquals(countBefore, countAfter)
    }

    @Test
    fun testSaveLocation_LocationDoesNotExists_CreateLocation() = runTest {
        val newLocationName = "New Location Name"
        val locationRepository = get<LocationRepository>()

        shopViewModel.hydrate(0)
        val countBefore: Int = locationRepository.getAll().count()
        shopViewModel.saveLocation(newLocationName, pinned, showDefaultAisle)
        val newLocation = locationRepository.getByName(newLocationName)
        val countAfter: Int = locationRepository.getAll().count()

        Assert.assertNotNull(newLocation)
        Assert.assertEquals(newLocationName, newLocation?.name)
        Assert.assertEquals(pinned, newLocation?.pinned)
        Assert.assertEquals(showDefaultAisle, newLocation?.showDefaultAisle)
        Assert.assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun testSaveLocation_SaveSuccessful_UiStateIsSuccess() = runTest {
        val updatedLocationName = "Updated Location Name"
        val existingLocation: Location = get<LocationRepository>().getAll().first()

        shopViewModel.hydrate(existingLocation.id)
        shopViewModel.saveLocation(updatedLocationName, pinned, showDefaultAisle)

        Assert.assertTrue(shopViewModel.shopUiState.value is ShopViewModel.ShopUiState.Success)
    }

    @Test
    fun testSaveLocation_AisleronErrorOnSave_UiStateIsError() = runTest {
        val existingLocation: Location =
            get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

        shopViewModel.hydrate(0)
        shopViewModel.saveLocation(existingLocation.name, pinned, showDefaultAisle)

        Assert.assertTrue(shopViewModel.shopUiState.value is ShopViewModel.ShopUiState.Error)
    }

    @Test
    fun testGetLocationName_LocationExists_ReturnsLocationName() = runTest {
        val existingLocation: Location = get<LocationRepository>().getAll().first()
        shopViewModel.hydrate(existingLocation.id)
        Assert.assertEquals(existingLocation.name, shopViewModel.locationName)
    }

    @Test
    fun testGetLocationName_LocationDoesNotExists_ReturnsNull() = runTest {
        shopViewModel.hydrate(0)
        Assert.assertNull(shopViewModel.locationName)
    }

    @Test
    fun testGetPinned_LocationExists_ReturnsLocationPinnedStatus() = runTest {
        val existingLocation: Location = get<LocationRepository>().getAll().first { it.pinned }
        shopViewModel.hydrate(existingLocation.id)
        Assert.assertEquals(existingLocation.pinned, shopViewModel.pinned)
    }

    @Test
    fun testGetPinned_LocationDoesNotExists_ReturnsFalse() = runTest {
        shopViewModel.hydrate(0)
        Assert.assertFalse(shopViewModel.pinned)
    }

    @Test
    fun testShowDefaultAisle_LocationExists_ReturnsLocationShowDefaultAisleStatus() = runTest {
        val existingLocation: Location =
            get<LocationRepository>().getAll().first { it.showDefaultAisle }
        shopViewModel.hydrate(existingLocation.id)
        Assert.assertEquals(existingLocation.showDefaultAisle, shopViewModel.showDefaultAisle)
    }

    @Test
    fun testShowDefaultAisle_LocationDoesNotExists_ReturnsTrue() = runTest {
        shopViewModel.hydrate(0)
        Assert.assertTrue(shopViewModel.showDefaultAisle)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ShopViewModelReturned() {
        val svm = ShopViewModel(
            get<AddLocationUseCase>(),
            get<UpdateLocationUseCase>(),
            get<GetLocationUseCase>(),
            get<AddLoyaltyCardUseCase>(),
            get<AddLoyaltyCardToLocationUseCase>(),
            get<RemoveLoyaltyCardFromLocationUseCase>(),
            get<GetLoyaltyCardForLocationUseCase>()
        )

        Assert.assertNotNull(svm)
    }

    @Test
    fun testSaveLocation_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on save Location"

        declare<AddLocationUseCase> {
            object : AddLocationUseCase {
                override suspend fun invoke(location: Location): Int {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val svm = get<ShopViewModel>()

        svm.hydrate(0)
        svm.saveLocation("Bogus Product", pinned, showDefaultAisle)

        Assert.assertTrue(svm.shopUiState.value is ShopViewModel.ShopUiState.Error)
        Assert.assertEquals(
            AisleronException.ExceptionCode.GENERIC_EXCEPTION,
            (svm.shopUiState.value as ShopViewModel.ShopUiState.Error).errorCode
        )
        Assert.assertEquals(
            exceptionMessage,
            (svm.shopUiState.value as ShopViewModel.ShopUiState.Error).errorMessage
        )
    }

    private suspend fun getLoyaltyCard(): LoyaltyCard {
        val loyaltyCard = LoyaltyCard(
            id = 0,
            name = "Loyalty Card Test",
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Dummy Intent"
        )

        val loyaltyCardId = get<LoyaltyCardRepository>().add(loyaltyCard)

        return loyaltyCard.copy(id = loyaltyCardId)
    }

    @Test
    fun hydrate_HasLoyaltyCard_LoyaltyCardNamePopulated() = runTest {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val loyaltyCard = getLoyaltyCard()
        get<LoyaltyCardRepository>().addToLocation(location.id, loyaltyCard.id)

        shopViewModel.hydrate(location.id)

        Assert.assertEquals(loyaltyCard.name, shopViewModel.loyaltyCardName)
    }

    @Test
    fun hydrate_HasNoLoyaltyCard_LoyaltyCardNameIsNull() = runTest {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

        shopViewModel.hydrate(location.id)

        Assert.assertNull(shopViewModel.loyaltyCardName)
    }

    @Test
    fun setLoyaltyCard_HasLoyaltyCard_LoyaltyCardNameUpdated() = runTest {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val loyaltyCard = getLoyaltyCard()
        shopViewModel.hydrate(location.id)

        shopViewModel.setLoyaltyCard(loyaltyCard)

        Assert.assertEquals(loyaltyCard.name, shopViewModel.loyaltyCardName)
    }

    @Test
    fun removeLoyaltyCard_MethodCalled_LoyaltyCardNameIsNull() = runTest {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val loyaltyCard = getLoyaltyCard()
        shopViewModel.hydrate(location.id)
        shopViewModel.setLoyaltyCard(loyaltyCard)

        shopViewModel.removeLoyaltyCard()

        Assert.assertNull(shopViewModel.loyaltyCardName)
    }

    @Test
    fun saveLocation_HasLoyaltyCard_LoyaltyCardSavedAgainstLocation() = runTest {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val loyaltyCard = getLoyaltyCard()
        shopViewModel.hydrate(location.id)
        shopViewModel.setLoyaltyCard(loyaltyCard)

        shopViewModel.saveLocation(location.name, pinned, showDefaultAisle)

        val savedLoyaltyCard = get<LoyaltyCardRepository>().getForLocation(location.id)

        Assert.assertEquals(loyaltyCard, savedLoyaltyCard)
    }

    @Test
    fun saveLocation_LoyaltyCardIsNull_RemoveLoyaltyCardFromLocation() = runTest {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val loyaltyCard = getLoyaltyCard()
        get<LoyaltyCardRepository>().addToLocation(location.id, loyaltyCard.id)
        shopViewModel.hydrate(location.id)
        shopViewModel.setLoyaltyCard(null)

        shopViewModel.saveLocation(location.name, pinned, showDefaultAisle)

        val savedLoyaltyCard = get<LoyaltyCardRepository>().getForLocation(location.id)

        Assert.assertNull(savedLoyaltyCard)
    }

    @Test
    fun saveLocation_IsNewLocation_LoyaltyCardSavedAgainstLocation() = runTest {
        val loyaltyCard = getLoyaltyCard()
        shopViewModel.hydrate(0)
        shopViewModel.setLoyaltyCard(loyaltyCard)

        shopViewModel.saveLocation("Test New Location With Loyalty Card", pinned, showDefaultAisle)

        val location = get<LocationRepository>().getByName("Test New Location With Loyalty Card")!!
        val savedLoyaltyCard = get<LoyaltyCardRepository>().getForLocation(location.id)

        Assert.assertEquals(loyaltyCard, savedLoyaltyCard)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            // parameters: pinned, showDefaultAisle
            return listOf(
                arrayOf(true, true),
                arrayOf(true, false),
                arrayOf(false, true),
                arrayOf(false, false)
            )
        }
    }
}