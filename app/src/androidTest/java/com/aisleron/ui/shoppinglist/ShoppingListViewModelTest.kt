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
        val updateShoppingListItem = AisleShoppingListItem(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = updatedAisleName,
            isDefault = existingAisle.isDefault,
            locationId = existingLocation.id,
            childCount = 0,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.updateAisle(updateShoppingListItem)

        val updatedAisle = runBlocking { testData.aisleRepository.get(existingAisle.id) }
        Assert.assertNotNull(updatedAisle)
        Assert.assertEquals(existingAisle.copy(name = updatedAisleName), updatedAisle)
    }

    @Test
    fun updateAisle_IsInvalidLocation_UiStateIsError() {
        val updatedAisleName = "Update Aisle Test"
        val existingAisle = runBlocking { testData.aisleRepository.getAll().first() }
        val updateShoppingListItem = AisleShoppingListItem(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = updatedAisleName,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = -1,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        shoppingListViewModel.hydrate(-1, FilterType.NEEDED)
        shoppingListViewModel.updateAisle(updateShoppingListItem)

        Assert.assertTrue(shoppingListViewModel.shoppingListUiState.value is ShoppingListViewModel.ShoppingListUiState.Error)
    }

    @Test
    fun removeItem_ItemIsStandardAisle_AisleRemoved() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val existingAisle = runBlocking {
            testData.aisleRepository.getAll()
                .first { it.locationId == existingLocation.id && !it.isDefault }
        }
        val shoppingListItem = AisleShoppingListItem(
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

        val removedAisle = runBlocking { testData.aisleRepository.get(existingAisle.id) }
        Assert.assertNull(removedAisle)
    }

    @Test
    fun removeItem_ItemIsInvalidAisle_NoAisleRemoved() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val shoppingListItem = AisleShoppingListItem(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = -1,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        val aisleCountBefore = runBlocking { testData.aisleRepository.getAll().count() }
        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.removeItem(shoppingListItem)
        val aisleCountAfter = runBlocking { testData.aisleRepository.getAll().count() }

        Assert.assertEquals(aisleCountBefore, aisleCountAfter)
    }

    @Test
    fun removeItem_ItemIsDefaultAisle_UiStateIsError() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val existingAisle = runBlocking {
            testData.aisleRepository.getAll()
                .first { it.locationId == existingLocation.id && it.isDefault }
        }
        val shoppingListItem = AisleShoppingListItem(
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
    fun removeItem_ItemIsProduct_ProductRemoved() {
        val locationId = runBlocking { testData.locationRepository.getAll().first().id }
        val shoppingList = runBlocking {
            testData.locationRepository.getLocationWithAislesWithProducts(locationId)
                .first()
        }
        val existingAisle = shoppingList!!.aisles[0]
        val aisleProduct = existingAisle.products.last()
        val existingProduct =
            runBlocking { testData.productRepository.get(aisleProduct.product.id)!! }
        val shoppingListItem = ProductShoppingListItem(
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
        shoppingListViewModel.removeItem(shoppingListItem)

        val removedProduct = runBlocking { testData.productRepository.get(existingProduct.id) }
        Assert.assertNull(removedProduct)
    }

    @Test
    fun removeItem_ItemIsInvalidProduct_NoProductRemoved() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val shoppingListItem = ProductShoppingListItem(
            aisleRank = 1000,
            rank = 1000,
            id = -1,
            name = "Dummy",
            inStock = false,
            aisleId = 1,
            aisleProductId = 1,
            updateAisleProductRankUseCase = testUseCases.updateAisleProductRankUseCase,
            removeProductUseCase = testUseCases.removeProductUseCase
        )

        val productCountBefore = runBlocking { testData.productRepository.getAll().count() }
        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.removeItem(shoppingListItem)
        val productCountAfter = runBlocking { testData.productRepository.getAll().count() }

        Assert.assertEquals(productCountBefore, productCountAfter)
    }

    @Test
    fun updateItemRank_ItemIsAisle_AisleRankUpdated() {
        val existingLocation = runBlocking { testData.locationRepository.getAll().first() }
        val existingAisle = runBlocking {
            testData.aisleRepository.getAll()
                .first { it.locationId == existingLocation.id && !it.isDefault }
        }
        val rankIncrement = 5
        val shoppingListItem = AisleShoppingListItem(
            rank = existingAisle.rank + rankIncrement,
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
        shoppingListViewModel.updateItemRank(shoppingListItem)

        val updatedAisle = runBlocking { testData.aisleRepository.get(existingAisle.id) }
        Assert.assertEquals(
            existingAisle.copy(rank = existingAisle.rank + rankIncrement),
            updatedAisle
        )
    }

    @Test
    fun updateItemRank_ItemIsProduct_ProductRankUpdated() {
        val locationId = runBlocking { testData.locationRepository.getAll().first().id }
        val shoppingList = runBlocking {
            testData.locationRepository.getLocationWithAislesWithProducts(locationId)
                .first()
        }
        val existingAisle = shoppingList!!.aisles[0]
        val aisleProduct = existingAisle.products.last()
        val rankIncrement = 5
        val existingProduct =
            runBlocking { testData.productRepository.get(aisleProduct.product.id)!! }
        val shoppingListItem = ProductShoppingListItem(
            aisleRank = existingAisle.rank,
            rank = aisleProduct.rank + rankIncrement,
            id = existingProduct.id,
            name = existingProduct.name,
            inStock = existingProduct.inStock,
            aisleId = existingAisle.id,
            aisleProductId = aisleProduct.id,
            updateAisleProductRankUseCase = testUseCases.updateAisleProductRankUseCase,
            removeProductUseCase = testUseCases.removeProductUseCase
        )

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateItemRank(shoppingListItem)

        val updatedAisleProduct =
            runBlocking { testData.aisleProductRepository.get(aisleProduct.id) }
        Assert.assertEquals(
            aisleProduct.copy(rank = aisleProduct.rank + rankIncrement),
            updatedAisleProduct
        )
    }

    @Test
    fun updateItemRank_ProductMovedAisle_ProductAisleUpdated() {
        val locationId = runBlocking { testData.locationRepository.getAll().first().id }
        val shoppingList = runBlocking {
            testData.locationRepository.getLocationWithAislesWithProducts(locationId)
                .first()
        }
        val existingAisle = shoppingList!!.aisles[0]
        val aisleProduct = existingAisle.products.last()
        val existingProduct =
            runBlocking { testData.productRepository.get(aisleProduct.product.id)!! }
        val newAisle = runBlocking {
            val newAisleId = testData.aisleRepository.add(
                Aisle(
                    "Dummy Aisle", emptyList(), locationId, 1000, -1, false
                )
            )
            testData.aisleRepository.get(newAisleId)!!
        }

        val shoppingListItem = ProductShoppingListItem(
            aisleRank = newAisle.rank,
            rank = aisleProduct.rank,
            id = existingProduct.id,
            name = existingProduct.name,
            inStock = existingProduct.inStock,
            aisleId = newAisle.id,
            aisleProductId = aisleProduct.id,
            updateAisleProductRankUseCase = testUseCases.updateAisleProductRankUseCase,
            removeProductUseCase = testUseCases.removeProductUseCase
        )

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateItemRank(shoppingListItem)

        val updatedAisleProduct =
            runBlocking { testData.aisleProductRepository.get(aisleProduct.id) }
        Assert.assertEquals(newAisle.id, updatedAisleProduct?.aisleId)
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
        val shoppingListItem = ProductShoppingListItem(
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
        val shoppingListItem = ProductShoppingListItem(
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

        val sli = AisleShoppingListItem(
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
        vm.updateAisle(sli)

        val uiState = vm.shoppingListUiState.value
        Assert.assertTrue(uiState is ShoppingListViewModel.ShoppingListUiState.Error)
        with(uiState as ShoppingListViewModel.ShoppingListUiState.Error) {
            Assert.assertEquals(AisleronException.GENERIC_EXCEPTION, this.errorCode)
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
            Assert.assertEquals(AisleronException.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun removeItem_ExceptionRaised_UiStateIsError() {
        val testUseCases = TestUseCaseProvider(testData)
        val exceptionMessage = "Error on Remove Item"
        val vm = ShoppingListViewModel(
            testUseCases.getShoppingListUseCase,
            testUseCases.updateProductStatusUseCase,
            testUseCases.addAisleUseCase,
            testUseCases.updateAisleUseCase,
            testUseCases.updateAisleProductRankUseCase,
            testUseCases.updateAisleRankUseCase,
            testUseCases.removeAisleUseCase,
            testUseCases.removeProductUseCase,
            object : GetAisleUseCase {
                override suspend operator fun invoke(id: Int): Aisle? {
                    throw Exception(exceptionMessage)
                }
            },
            TestScope(UnconfinedTestDispatcher())
        )

        vm.hydrate(1, FilterType.NEEDED)
        val sli = AisleShoppingListItem(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = 1,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = object : GetAisleUseCase {
                override suspend operator fun invoke(id: Int): Aisle? {
                    throw Exception(exceptionMessage)
                }
            },
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )
        vm.removeItem(sli)

        val uiState = vm.shoppingListUiState.value
        Assert.assertTrue(uiState is ShoppingListViewModel.ShoppingListUiState.Error)
        with(uiState as ShoppingListViewModel.ShoppingListUiState.Error) {
            Assert.assertEquals(AisleronException.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

//Hydrate, each filter type
}