package com.aisleron.ui.shop

import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.AddLocationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(value = Parameterized::class)
class ShopViewModelTest(private val pinned: Boolean) {
    private lateinit var testData: TestDataManager
    private lateinit var shopViewModel: ShopViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        testData = TestDataManager()
        val testUseCases = TestUseCaseProvider(testData)
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        shopViewModel = ShopViewModel(
            testUseCases.addLocationUseCase,
            testUseCases.updateLocationUseCase,
            testUseCases.getLocationUseCase,
            testScope
        )
    }

    @Test
    fun testSaveLocation_LocationExists_UpdateLocation() = runTest {
        val updatedLocationName = "Updated Location Name"
        val existingLocation: Location = testData.locationRepository.getAll().first()
        val countBefore: Int = testData.locationRepository.getAll().count()

        shopViewModel.hydrate(existingLocation.id)
        shopViewModel.saveLocation(updatedLocationName, pinned)

        val updatedLocation = testData.locationRepository.get(existingLocation.id)
        val countAfter: Int = testData.locationRepository.getAll().count()

        Assert.assertNotNull(updatedLocation)
        Assert.assertEquals(updatedLocationName, updatedLocation?.name)
        Assert.assertEquals(pinned, updatedLocation?.pinned)
        Assert.assertEquals(countBefore, countAfter)
    }

    @Test
    fun testSaveLocation_LocationDoesNotExists_CreateLocation() = runTest {
        val newLocationName = "New Location Name"

        shopViewModel.hydrate(0)
        val countBefore: Int = testData.locationRepository.getAll().count()
        shopViewModel.saveLocation(newLocationName, pinned)
        val newLocation = testData.locationRepository.getByName(newLocationName)
        val countAfter: Int = testData.locationRepository.getAll().count()

        Assert.assertNotNull(newLocation)
        Assert.assertEquals(newLocationName, newLocation?.name)
        Assert.assertEquals(pinned, newLocation?.pinned)
        Assert.assertEquals(newLocationName, shopViewModel.locationName)
        Assert.assertEquals(pinned, shopViewModel.pinned)
        Assert.assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun testSaveLocation_SaveSuccessful_UiStateIsSuccess() = runTest {
        val updatedLocationName = "Updated Location Name"
        val existingLocation: Location = testData.locationRepository.getAll().first()

        shopViewModel.hydrate(existingLocation.id)
        shopViewModel.saveLocation(updatedLocationName, pinned)

        Assert.assertEquals(
            ShopViewModel.ShopUiState.Updated(shopViewModel),
            shopViewModel.shopUiState.value
        )

        Assert.assertEquals(
            shopViewModel,
            (shopViewModel.shopUiState.value as ShopViewModel.ShopUiState.Updated).shop
        )
    }

    @Test
    fun testSaveLocation_AisleronErrorOnSave_UiStateIsError() = runTest {
        val existingLocation: Location = testData.locationRepository.getAll().first()

        shopViewModel.hydrate(0)
        shopViewModel.saveLocation(existingLocation.name, pinned)

        Assert.assertTrue(shopViewModel.shopUiState.value is ShopViewModel.ShopUiState.Error)
    }

    @Test
    fun testGetLocationName_LocationExists_ReturnsLocationName() = runTest {
        val existingLocation: Location = testData.locationRepository.getAll().first()
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
        val existingLocation: Location = testData.locationRepository.getAll().first { it.pinned }
        shopViewModel.hydrate(existingLocation.id)
        Assert.assertEquals(existingLocation.pinned, shopViewModel.pinned)
    }

    @Test
    fun testGetPinned_LocationDoesNotExists_ReturnsFalse() = runTest {
        shopViewModel.hydrate(0)
        Assert.assertFalse(shopViewModel.pinned)
    }

    @Test
    fun testGetDefaultFilter_LocationExists_ReturnsLocationFilter() = runTest {
        val existingLocation: Location = testData.locationRepository.getAll().first()
        shopViewModel.hydrate(existingLocation.id)
        Assert.assertEquals(existingLocation.defaultFilter, shopViewModel.defaultFilter)
    }

    @Test
    fun testGetDefaultFilter_LocationDoesNotExists_ReturnsNull() = runTest {
        shopViewModel.hydrate(0)
        Assert.assertNull(shopViewModel.defaultFilter)
    }

    @Test
    fun testGetType_LocationExists_ReturnsLocationType() = runTest {
        val existingLocation: Location = testData.locationRepository.getAll().first()
        shopViewModel.hydrate(existingLocation.id)
        Assert.assertEquals(existingLocation.type, shopViewModel.type)
    }

    @Test
    fun testGetType_LocationDoesNotExists_ReturnsNull() = runTest {
        shopViewModel.hydrate(0)
        Assert.assertNull(shopViewModel.type)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ShopViewModelReturned() {
        val testUseCases = TestUseCaseProvider(testData)
        val svm = ShopViewModel(
            testUseCases.addLocationUseCase,
            testUseCases.updateLocationUseCase,
            testUseCases.getLocationUseCase
        )

        Assert.assertNotNull(svm)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSaveLocation_ExceptionRaised_UiStateIsError() = runTest {
        val testUseCases = TestUseCaseProvider(testData)
        val exceptionMessage = "Error on save Location"
        val svm = ShopViewModel(
            object : AddLocationUseCase {
                override suspend fun invoke(location: Location): Int {
                    throw Exception(exceptionMessage)
                }
            },
            testUseCases.updateLocationUseCase,
            testUseCases.getLocationUseCase,
            TestScope(UnconfinedTestDispatcher())
        )

        svm.hydrate(0)
        svm.saveLocation("Bogus Product", pinned)

        Assert.assertTrue(svm.shopUiState.value is ShopViewModel.ShopUiState.Error)
        Assert.assertEquals(
            AisleronException.GENERIC_EXCEPTION,
            (svm.shopUiState.value as ShopViewModel.ShopUiState.Error).errorCode
        )
        Assert.assertEquals(
            exceptionMessage,
            (svm.shopUiState.value as ShopViewModel.ShopUiState.Error).errorMessage
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(true),
                arrayOf(false)
            )
        }
    }
}