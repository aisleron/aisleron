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

package com.aisleron.ui.shoppinglist

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.assertTrue

class ShoppingListViewModelTest : KoinTest {
    private lateinit var shoppingListViewModel: ShoppingListViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        shoppingListViewModel = get<ShoppingListViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun hydrate_IsValidLocation_LocationMembersAreCorrect() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
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
    fun hydrate_LocationHasNoAisles_ShoppingListIsEmpty() = runTest {
        val location = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "No Aisle Shop",
            pinned = false,
            aisles = emptyList(),
            showDefaultAisle = true
        )

        val locationId = get<LocationRepository>().add(location)
        shoppingListViewModel.hydrate(locationId, location.defaultFilter)

        assertTrue((shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList.isEmpty())
    }

    @Test
    fun addAisle_IsValidLocation_AisleAdded() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val newAisleName = "Add New Aisle Test"

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.addAisle(newAisleName)

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }

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
    fun updateAisle_IsValidLocation_AisleAdded() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val updatedAisleName = "Update Aisle Test"
        val aisleRepository = get<AisleRepository>()
        val existingAisle = aisleRepository.getAll().first()
        val updateShoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            locationId = existingLocation.id,
            childCount = 0,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.updateAisleName(updateShoppingListItem, updatedAisleName)

        val updatedAisle = aisleRepository.get(existingAisle.id)
        Assert.assertNotNull(updatedAisle)
        Assert.assertEquals(existingAisle.copy(name = updatedAisleName), updatedAisle)
    }

    @Test
    fun updateAisle_IsInvalidLocation_UiStateIsError() = runTest {
        val updatedAisleName = "Update Aisle Test"
        val existingAisle = get<AisleRepository>().getAll().first()
        val updateShoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = -1,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        shoppingListViewModel.hydrate(-1, FilterType.NEEDED)
        shoppingListViewModel.updateAisleName(updateShoppingListItem, updatedAisleName)

        Assert.assertTrue(shoppingListViewModel.shoppingListUiState.value is ShoppingListViewModel.ShoppingListUiState.Error)
    }

    @Test
    fun removeItem_ItemIsDefaultAisle_UiStateIsError() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val existingAisle = get<AisleRepository>().getAll()
            .first { it.locationId == existingLocation.id && it.isDefault }

        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = existingLocation.id,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.removeItem(shoppingListItem)

        Assert.assertTrue(shoppingListViewModel.shoppingListUiState.value is ShoppingListViewModel.ShoppingListUiState.Error)
    }

    @Test
    fun updateProductStatus_InStockTrue_ProductUpdatedToInStock() = runTest {
        val locationRepository = get<LocationRepository>()
        val locationId = locationRepository.getAll().first { it.type == LocationType.SHOP }.id
        val shoppingList = locationRepository.getLocationWithAislesWithProducts(locationId).first()
        val newInStock = true
        val existingAisle =
            shoppingList!!.aisles.first { it.products.count { p -> !p.product.inStock } > 0 }
        val aisleProduct = existingAisle.products.first { it.product.inStock == !newInStock }
        val productRepository = get<ProductRepository>()
        val existingProduct = productRepository.get(aisleProduct.product.id)!!
        val shoppingListItem = ProductShoppingListItemViewModel(
            aisleRank = existingAisle.rank,
            rank = aisleProduct.rank,
            id = existingProduct.id,
            name = existingProduct.name,
            inStock = existingProduct.inStock,
            aisleId = existingAisle.id,
            aisleProductId = aisleProduct.id,
            updateAisleProductRankUseCase = get<UpdateAisleProductRankUseCase>(),
            removeProductUseCase = get<RemoveProductUseCase>()
        )

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateProductStatus(shoppingListItem, newInStock)

        val updatedProduct = productRepository.get(existingProduct.id)
        Assert.assertEquals(
            existingProduct.copy(inStock = newInStock),
            updatedProduct
        )
    }

    @Test
    fun updateProductStatus_InStockFalse_ProductUpdatedToNotInStock() = runTest {
        val locationRepository = get<LocationRepository>()
        val locationId = locationRepository.getAll().first().id
        val shoppingList = locationRepository.getLocationWithAislesWithProducts(locationId).first()
        val newInStock = false
        val existingAisle = shoppingList!!.aisles[0]
        val aisleProduct = existingAisle.products.first { it.product.inStock == !newInStock }
        val productRepository = get<ProductRepository>()
        val existingProduct = productRepository.get(aisleProduct.product.id)!!
        val shoppingListItem = ProductShoppingListItemViewModel(
            aisleRank = existingAisle.rank,
            rank = aisleProduct.rank,
            id = existingProduct.id,
            name = existingProduct.name,
            inStock = existingProduct.inStock,
            aisleId = existingAisle.id,
            aisleProductId = aisleProduct.id,
            updateAisleProductRankUseCase = get<UpdateAisleProductRankUseCase>(),
            removeProductUseCase = get<RemoveProductUseCase>()
        )

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateProductStatus(shoppingListItem, newInStock)

        val updatedProduct = productRepository.get(existingProduct.id)
        Assert.assertEquals(
            existingProduct.copy(inStock = newInStock),
            updatedProduct
        )
    }

    @Test
    fun submitProductSearch_ProductsMatch_UiStateHasProducts() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val searchString = "Apple"
        val productSearchCount =
            get<ProductRepository>().getAll().count { it.name.contains(searchString) }

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
    fun submitProductSearch_NoProductsMatch_UiStateHasNoProducts() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
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
    fun submitProductSearch_SearchRun_UiStateHasAllAisles() = runTest {
        val locationRepository = get<LocationRepository>()
        val locationId = locationRepository.getAll().first().id
        val location = locationRepository.getLocationWithAislesWithProducts(locationId).first()!!
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
    fun requestDefaultList_SearchRun_UiStateHasAllAislesAndProducts() = runTest {
        val locationRepository = get<LocationRepository>()
        val locationId = locationRepository.getAll().first().id
        val location = locationRepository.getLocationWithAislesWithProducts(locationId).first()!!
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
        val vm = ShoppingListViewModel(
            get<GetShoppingListUseCase>(),
            get<UpdateProductStatusUseCase>(),
            get<AddAisleUseCase>(),
            get<UpdateAisleUseCase>(),
            get<UpdateAisleProductRankUseCase>(),
            get<UpdateAisleRankUseCase>(),
            get<RemoveAisleUseCase>(),
            get<RemoveProductUseCase>(),
            get<GetAisleUseCase>()
        )

        Assert.assertNotNull(vm)
    }

    @Test
    fun updateAisle_ExceptionRaised_UiStateIsError() {

        val exceptionMessage = "Error on update Aisle"

        declare<UpdateAisleUseCase> {
            object : UpdateAisleUseCase {
                override suspend fun invoke(aisle: Aisle) {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<ShoppingListViewModel>()

        vm.hydrate(1, FilterType.NEEDED)

        val sli = AisleShoppingListItemViewModel(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = 1,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )
        vm.updateAisleName(sli, "Dummy Dummy")

        val uiState = vm.shoppingListUiState.value
        Assert.assertTrue(uiState is ShoppingListViewModel.ShoppingListUiState.Error)
        with(uiState as ShoppingListViewModel.ShoppingListUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @Test
    fun addAisle_ExceptionRaised_UiStateIsError() {
        val exceptionMessage = "Error on update Product Status"

        declare<AddAisleUseCase> {
            object : AddAisleUseCase {
                override suspend fun invoke(aisle: Aisle): Int {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<ShoppingListViewModel>()

        vm.hydrate(1, FilterType.NEEDED)
        vm.addAisle("New Dummy Aisle")

        val uiState = vm.shoppingListUiState.value
        Assert.assertTrue(uiState is ShoppingListViewModel.ShoppingListUiState.Error)
        with(uiState as ShoppingListViewModel.ShoppingListUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @Test
    fun removeItem_ExceptionRaised_UiStateIsError() {
        val exceptionMessage = "Error on Remove Item"

        declare<GetAisleUseCase> {
            object : GetAisleUseCase {
                override suspend operator fun invoke(id: Int): Aisle? {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<ShoppingListViewModel>()

        vm.hydrate(1, FilterType.NEEDED)
        val sli = AisleShoppingListItemViewModel(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = 1,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
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
    fun updateItemRank_ItemIsAisle_AisleRankUpdated() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val aisleRepository = get<AisleRepository>()
        val movedAisle = aisleRepository.getAll()
            .last { it.locationId == existingLocation.id && !it.isDefault }

        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = movedAisle.rank,
            id = movedAisle.id,
            name = movedAisle.name,
            isDefault = movedAisle.isDefault,
            childCount = 0,
            locationId = movedAisle.locationId,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        val precedingAisle = aisleRepository.getAll()
            .first { it.locationId == movedAisle.locationId && !it.isDefault && it.id != movedAisle.id }

        val precedingItem = AisleShoppingListItemViewModel(
            rank = precedingAisle.rank,
            id = precedingAisle.id,
            name = precedingAisle.name,
            isDefault = precedingAisle.isDefault,
            childCount = 0,
            locationId = precedingAisle.locationId,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.updateItemRank(shoppingListItem, precedingItem)

        val updatedAisle = aisleRepository.get(movedAisle.id)
        Assert.assertEquals(precedingItem.rank + 1, updatedAisle?.rank)
    }

    @Test
    fun removeItem_ItemIsStandardAisle_AisleRemoved() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val aisleRepository = get<AisleRepository>()
        val existingAisle = aisleRepository.getAll()
            .last { it.locationId == existingLocation.id && !it.isDefault }

        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = existingAisle.locationId,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.removeItem(shoppingListItem)

        val removedAisle = aisleRepository.get(existingAisle.id)
        Assert.assertNull(removedAisle)
    }

    /**
     * TODO: Add tests for showDefaultAisle
     */


}