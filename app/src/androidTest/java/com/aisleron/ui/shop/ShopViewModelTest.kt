package com.aisleron.ui.shop

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ShopViewModelTest {

    private lateinit var testData: TestDataManager
    private lateinit var shopViewModel: ShopViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        testData = TestDataManager()

        val addAisleProductsUseCase = AddAisleProductsUseCase(testData.aisleProductRepository)
        val isLocationNameUniqueUseCase = IsLocationNameUniqueUseCase(testData.locationRepository)
        val addAisleUseCase = AddAisleUseCase(testData.aisleRepository)
        val getAllProductsUseCase = GetAllProductsUseCase(testData.productRepository)

        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        shopViewModel = ShopViewModel(
            AddLocationUseCase(
                testData.locationRepository,
                addAisleUseCase,
                getAllProductsUseCase,
                addAisleProductsUseCase,
                isLocationNameUniqueUseCase
            ),
            UpdateLocationUseCase(testData.locationRepository, isLocationNameUniqueUseCase),
            GetLocationUseCase(testData.locationRepository),
            testScope
        )
    }

    @Test
    fun testSaveLocation_LocationExists_UpdateLocation() = runTest {
        val updatedLocationName = "Updated Location Name"
        val existingLocation: Location = testData.locationRepository.getAll().first()
        val countBefore: Int = testData.locationRepository.getAll().count()

        shopViewModel.hydrate(existingLocation.id)
        shopViewModel.saveLocation(updatedLocationName, !existingLocation.pinned)

        val updatedLocation = testData.locationRepository.get(existingLocation.id)
        val countAfter: Int = testData.locationRepository.getAll().count()

        Assert.assertNotNull(updatedLocation)
        Assert.assertEquals(updatedLocationName, updatedLocation?.name)
        Assert.assertEquals(!existingLocation.pinned, updatedLocation?.pinned)
        Assert.assertEquals(countBefore, countAfter)
    }

    @Test
    fun testSaveLocation_LocationDoesNotExists_CreateLocation() = runTest {
        val newLocationName = "New Location Name"
        val pinned = false

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
        shopViewModel.saveLocation(updatedLocationName, !existingLocation.pinned)

        Assert.assertEquals(
            ShopViewModel.ShopUiState.Updated(shopViewModel),
            shopViewModel.shopUiState.value
        )
    }

    @Test
    fun testSaveLocation_AisleronErrorOnSave_UiStateIsError() = runTest {
        val existingLocation: Location = testData.locationRepository.getAll().first()

        shopViewModel.hydrate(0)
        shopViewModel.saveLocation(existingLocation.name, false)

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
}