package com.aisleron.ui.shoppinglist

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class ShoppingListViewModelTest {
    private lateinit var testData: TestDataManager
    private lateinit var shoppingListViewModel: ShoppingListViewModel
    private lateinit var testUseCases: TestUseCaseProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        testData = TestDataManager()
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        testUseCases = TestUseCaseProvider(testData)

        shoppingListViewModel = ShoppingListViewModel(
            testUseCases.getShoppingListUseCase,
            testUseCases.updateProductStatusUseCase,
            testUseCases.addAisleUseCase,
            testUseCases.updateAisleUseCase,
            testUseCases.updateAisleProductRankUseCase,
            testUseCases.updateAisleRankUseCase,
            testUseCases.removeAisleUseCase,
            testUseCases.removeProductUseCase,
            testUseCases.getAisleUseCase,
            testScope
        )
    }

    @Test
    fun hydrate_IsValidLocation_LocationMembersAreCorrect() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)

        Assert.assertEquals(existingLocation.name, shoppingListViewModel.locationName)
        Assert.assertEquals(existingLocation.defaultFilter, shoppingListViewModel.defaultFilter)
        Assert.assertEquals(existingLocation.type, shoppingListViewModel.locationType)
    }

    @Test
    fun hydrate_IsInvalidLocation_LocationMembersAreDefault() {
        shoppingListViewModel.hydrate(-1, FilterType.NEEDED)

        Assert.assertEquals("", shoppingListViewModel.locationName)
        Assert.assertEquals(FilterType.NEEDED, shoppingListViewModel.defaultFilter)
        Assert.assertEquals(LocationType.HOME, shoppingListViewModel.locationType)
    }

    @Test
    fun hydrate_LocationHasNoAisles_ShoppingListIsEmpty() {
        val location = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "No Aisle Shop",
            pinned = false,
            aisles = emptyList()
        )

        val locationId = runBlocking { testData.locationRepository.add(location) }
        shoppingListViewModel.hydrate(locationId, location.defaultFilter)

        assertTrue((shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList.isEmpty())
    }

    @Test
    fun addAisle_IsValidLocation_AisleAdded() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val newAisleName = "Add New Aisle Test"

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.addAisle(newAisleName)

        val addedAisle = runBlocking {
            testData.aisleRepository.getAll().firstOrNull { it.name == newAisleName }
        }

        Assert.assertNotNull(addedAisle)
        Assert.assertEquals(newAisleName, addedAisle?.name)
        Assert.assertEquals(existingLocation.id, addedAisle?.locationId)
        Assert.assertFalse(addedAisle!!.isDefault)
    }

    @Test
    fun addAisle_IsInvalidLocation_UiStateIsError() {
        val newAisleName = "Add New Aisle Test"

        shoppingListViewModel.hydrate(-1, FilterType.NEEDED)
        shoppingListViewModel.addAisle(newAisleName)

        Assert.assertTrue(shoppingListViewModel.shoppingListUiState.value is ShoppingListViewModel.ShoppingListUiState.Error)
    }

    @Test
    fun updateAisle_IsValidLocation_AisleAdded() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val updatedAisleName = "Update Aisle Test"
        val existingAisle = runBlocking { testData.aisleRepository.getAll().first() }
        val updateShoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            locationId = existingLocation.id,
            childCount = 0,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.updateAisleName(updateShoppingListItem, updatedAisleName)

        val updatedAisle = runBlocking { testData.aisleRepository.get(existingAisle.id) }
        Assert.assertNotNull(updatedAisle)
        Assert.assertEquals(existingAisle.copy(name = updatedAisleName), updatedAisle)
    }

    @Test
    fun updateAisle_IsInvalidLocation_UiStateIsError() {
        val updatedAisleName = "Update Aisle Test"
        val existingAisle = runBlocking { testData.aisleRepository.getAll().first() }
        val updateShoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = -1,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        shoppingListViewModel.hydrate(-1, FilterType.NEEDED)
        shoppingListViewModel.updateAisleName(updateShoppingListItem, updatedAisleName)

        Assert.assertTrue(shoppingListViewModel.shoppingListUiState.value is ShoppingListViewModel.ShoppingListUiState.Error)
    }

    @Test
    fun removeItem_ItemIsDefaultAisle_UiStateIsError() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val existingAisle = runBlocking {
            testData.aisleRepository.getAll()
                .first { it.locationId == existingLocation.id && it.isDefault }
        }
        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = existingLocation.id,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.removeItem(shoppingListItem)

        Assert.assertTrue(shoppingListViewModel.shoppingListUiState.value is ShoppingListViewModel.ShoppingListUiState.Error)
    }

    @Test
    fun updateProductStatus_InStockTrue_ProductUpdatedToInStock() {
        val locationId = runBlocking { testData.locationRepository.getAll().first().id }
        val shoppingList = runBlocking {
            testData.locationRepository.getLocationWithAislesWithProducts(locationId)
                .first()
        }
        val newInStock = true
        val existingAisle = shoppingList!!.aisles[0]
        val aisleProduct = existingAisle.products.first { it.product.inStock == !newInStock }
        val existingProduct =
            runBlocking { testData.productRepository.get(aisleProduct.product.id)!! }
        val shoppingListItem = ProductShoppingListItemViewModel(
            aisleRank = existingAisle.rank,
            rank = aisleProduct.rank,
            id = existingProduct.id,
            name = existingProduct.name,
            inStock = existingProduct.inStock,
            aisleId = existingAisle.id,
            aisleProductId = aisleProduct.id,
            updateAisleProductRankUseCase = testUseCases.updateAisleProductRankUseCase,
            removeProductUseCase = testUseCases.removeProductUseCase
        )

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateProductStatus(shoppingListItem, newInStock)

        val updatedProduct =
            runBlocking { testData.productRepository.get(existingProduct.id) }
        Assert.assertEquals(
            existingProduct.copy(inStock = newInStock),
            updatedProduct
        )
    }

    @Test
    fun updateProductStatus_InStockFalse_ProductUpdatedToNotInStock() {
        val locationId = runBlocking { testData.locationRepository.getAll().first().id }
        val shoppingList = runBlocking {
            testData.locationRepository.getLocationWithAislesWithProducts(locationId)
                .first()
        }
        val newInStock = false
        val existingAisle = shoppingList!!.aisles[0]
        val aisleProduct = existingAisle.products.first { it.product.inStock == !newInStock }
        val existingProduct =
            runBlocking { testData.productRepository.get(aisleProduct.product.id)!! }
        val shoppingListItem = ProductShoppingListItemViewModel(
            aisleRank = existingAisle.rank,
            rank = aisleProduct.rank,
            id = existingProduct.id,
            name = existingProduct.name,
            inStock = existingProduct.inStock,
            aisleId = existingAisle.id,
            aisleProductId = aisleProduct.id,
            updateAisleProductRankUseCase = testUseCases.updateAisleProductRankUseCase,
            removeProductUseCase = testUseCases.removeProductUseCase
        )

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateProductStatus(shoppingListItem, newInStock)

        val updatedProduct =
            runBlocking { testData.productRepository.get(existingProduct.id) }
        Assert.assertEquals(
            existingProduct.copy(inStock = newInStock),
            updatedProduct
        )
    }


    @Test
    fun submitProductSearch_ProductsMatch_UiStateHasProducts() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val searchString = "4"
        val productSearchCount = runBlocking {
            testData.productRepository.getAll().count { it.name.contains(searchString) }
        }

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.submitProductSearch(searchString)

        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList
        Assert.assertEquals(
            productSearchCount,
            shoppingList.count { p -> p.name.contains(searchString) && p.itemType == ShoppingListItem.ItemType.PRODUCT }
        )

    }

    @Test
    fun submitProductSearch_NoProductsMatch_UiStateHasNoProducts() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val searchString = "No Product Name Matches This String Woo Yeah"
        val productSearchCount = 0

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.submitProductSearch(searchString)

        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList
        Assert.assertEquals(
            productSearchCount,
            shoppingList.count { p -> p.name.contains(searchString) && p.itemType == ShoppingListItem.ItemType.PRODUCT }
        )

    }

    @Test
    fun submitProductSearch_SearchRun_UiStateHasAllAisles() {
        val locationId = runBlocking { testData.locationRepository.getAll().first().id }
        val location = runBlocking {
            testData.locationRepository.getLocationWithAislesWithProducts(locationId).first()
        }!!
        val searchString = "No Product Name Matches This String Woo Yeah"
        val aisleCount = location.aisles.count()

        shoppingListViewModel.hydrate(location.id, location.defaultFilter)
        shoppingListViewModel.submitProductSearch(searchString)

        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList
        Assert.assertEquals(
            aisleCount, shoppingList.count { it.itemType == ShoppingListItem.ItemType.AISLE }
        )
    }

    @Test
    fun requestDefaultList_SearchRun_UiStateHasAllAislesAndProducts() {
        val locationId = runBlocking { testData.locationRepository.getAll().first().id }
        val location = runBlocking {
            testData.locationRepository.getLocationWithAislesWithProducts(locationId).first()
        }!!
        val aisleCount = location.aisles.count()
        var productCount = 0
        location.aisles.forEach {
            productCount += it.products.count()
        }

        shoppingListViewModel.hydrate(location.id, FilterType.ALL)
        shoppingListViewModel.requestDefaultList()

        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList
        Assert.assertEquals(
            aisleCount, shoppingList.count { it.itemType == ShoppingListItem.ItemType.AISLE }
        )
        Assert.assertEquals(
            productCount, shoppingList.count { it.itemType == ShoppingListItem.ItemType.PRODUCT }
        )
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ShoppingListViewModelReturned() {
        val testUseCases = TestUseCaseProvider(testData)
        val vm = ShoppingListViewModel(
            testUseCases.getShoppingListUseCase,
            testUseCases.updateProductStatusUseCase,
            testUseCases.addAisleUseCase,
            testUseCases.updateAisleUseCase,
            testUseCases.updateAisleProductRankUseCase,
            testUseCases.updateAisleRankUseCase,
            testUseCases.removeAisleUseCase,
            testUseCases.removeProductUseCase,
            testUseCases.getAisleUseCase
        )

        Assert.assertNotNull(vm)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateAisle_ExceptionRaised_UiStateIsError() {
        val testUseCases = TestUseCaseProvider(testData)
        val exceptionMessage = "Error on update Aisle"
        val vm = ShoppingListViewModel(
            testUseCases.getShoppingListUseCase,
            testUseCases.updateProductStatusUseCase,
            testUseCases.addAisleUseCase,
            object : UpdateAisleUseCase {
                override suspend fun invoke(aisle: Aisle) {
                    throw Exception(exceptionMessage)
                }
            },
            testUseCases.updateAisleProductRankUseCase,
            testUseCases.updateAisleRankUseCase,
            testUseCases.removeAisleUseCase,
            testUseCases.removeProductUseCase,
            testUseCases.getAisleUseCase,
            TestScope(UnconfinedTestDispatcher())
        )

        vm.hydrate(1, FilterType.NEEDED)

        val sli = AisleShoppingListItemViewModel(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = 1,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )
        vm.updateAisleName(sli, "Dummy Dummy")

        val uiState = vm.shoppingListUiState.value
        Assert.assertTrue(uiState is ShoppingListViewModel.ShoppingListUiState.Error)
        with(uiState as ShoppingListViewModel.ShoppingListUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAisle_ExceptionRaised_UiStateIsError() {
        val testUseCases = TestUseCaseProvider(testData)
        val exceptionMessage = "Error on update Product Status"
        val vm = ShoppingListViewModel(
            testUseCases.getShoppingListUseCase,
            testUseCases.updateProductStatusUseCase,
            object : AddAisleUseCase {
                override suspend fun invoke(aisle: Aisle): Int {
                    throw Exception(exceptionMessage)
                }
            },
            testUseCases.updateAisleUseCase,
            testUseCases.updateAisleProductRankUseCase,
            testUseCases.updateAisleRankUseCase,
            testUseCases.removeAisleUseCase,
            testUseCases.removeProductUseCase,
            testUseCases.getAisleUseCase,
            TestScope(UnconfinedTestDispatcher())
        )

        vm.hydrate(1, FilterType.NEEDED)
        vm.addAisle("New Dummy Aisle")

        val uiState = vm.shoppingListUiState.value
        Assert.assertTrue(uiState is ShoppingListViewModel.ShoppingListUiState.Error)
        with(uiState as ShoppingListViewModel.ShoppingListUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun removeItem_ExceptionRaised_UiStateIsError() {
        val testUseCases = TestUseCaseProvider(testData)
        val exceptionMessage = "Error on Remove Item"
        val getAisleUseCase = object : GetAisleUseCase {
            override suspend operator fun invoke(id: Int): Aisle? {
                throw Exception(exceptionMessage)
            }
        }

        val vm = ShoppingListViewModel(
            testUseCases.getShoppingListUseCase,
            testUseCases.updateProductStatusUseCase,
            testUseCases.addAisleUseCase,
            testUseCases.updateAisleUseCase,
            testUseCases.updateAisleProductRankUseCase,
            testUseCases.updateAisleRankUseCase,
            testUseCases.removeAisleUseCase,
            testUseCases.removeProductUseCase,
            getAisleUseCase,
            TestScope(UnconfinedTestDispatcher())
        )

        vm.hydrate(1, FilterType.NEEDED)
        val sli = AisleShoppingListItemViewModel(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = 1,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )
        vm.removeItem(sli)

        val uiState = vm.shoppingListUiState.value
        Assert.assertTrue(uiState is ShoppingListViewModel.ShoppingListUiState.Error)
        with(uiState as ShoppingListViewModel.ShoppingListUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @Test
    fun updateItemRank_ItemIsAisle_AisleRankUpdated() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val movedAisle = runBlocking {
            testData.aisleRepository.getAll()
                .last { it.locationId == existingLocation.id && !it.isDefault }
        }

        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = movedAisle.rank,
            id = movedAisle.id,
            name = movedAisle.name,
            isDefault = movedAisle.isDefault,
            childCount = 0,
            locationId = movedAisle.locationId,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        val precedingAisle = runBlocking {
            testData.aisleRepository.getAll()
                .first { it.locationId == movedAisle.locationId && !it.isDefault && it.id != movedAisle.id }
        }

        val precedingItem = AisleShoppingListItemViewModel(
            rank = precedingAisle.rank,
            id = precedingAisle.id,
            name = precedingAisle.name,
            isDefault = precedingAisle.isDefault,
            childCount = 0,
            locationId = precedingAisle.locationId,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.updateItemRank(shoppingListItem, precedingItem)

        val updatedAisle = runBlocking { testData.aisleRepository.get(movedAisle.id) }
        Assert.assertEquals(precedingItem.rank + 1, updatedAisle?.rank)
    }

    @Test
    fun removeItem_ItemIsStandardAisle_AisleRemoved() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val existingAisle = runBlocking {
            testData.aisleRepository.getAll()
                .last { it.locationId == existingLocation.id && !it.isDefault }
        }

        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = existingAisle.locationId,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.removeItem(shoppingListItem)

        val removedAisle = runBlocking { testData.aisleRepository.get(existingAisle.id) }
        Assert.assertNull(removedAisle)
    }


}