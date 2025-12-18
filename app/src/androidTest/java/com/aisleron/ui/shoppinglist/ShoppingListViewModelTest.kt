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
import com.aisleron.domain.aisle.usecase.ExpandCollapseAislesForLocationUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAislesForLocationUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleExpandedUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.SortLocationByNameUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.domain.loyaltycard.usecase.GetLoyaltyCardForLocationUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductQtyNeededUseCase
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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
    fun hydrate_LocationHasNoAislesOrProducts_EmptyListItemExcluded() = runTest {
        val location = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "No Aisle Shop",
            pinned = false,
            aisles = emptyList(),
            showDefaultAisle = false
        )

        val locationId = get<LocationRepository>().add(location)
        shoppingListViewModel.hydrate(locationId, location.defaultFilter)

        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList

        assertEquals(1, shoppingList.count())
        assertEquals(1, shoppingList.count { it.itemType == ShoppingListItem.ItemType.EMPTY_LIST })
    }

    @Test
    fun hydrate_ListHasAislesAndProducts_EmptyListItemExcluded() = runTest {
        val location = getShoppingList()
        shoppingListViewModel.hydrate(location.id, location.defaultFilter)

        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList

        assertTrue(shoppingList.isNotEmpty())
        assertEquals(0, shoppingList.count { it.itemType == ShoppingListItem.ItemType.EMPTY_LIST })
    }

    @Test
    fun removeItem_ItemIsDefaultAisle_UiStateIsError() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val existingAisle = get<AisleRepository>().getAll()
            .first { it.locationId == existingLocation.id && it.isDefault }

        val shoppingListItem = getAisleShoppingListItemViewModel(existingAisle)

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.removeItem(shoppingListItem)

        Assert.assertTrue(shoppingListViewModel.shoppingListUiState.value is ShoppingListViewModel.ShoppingListUiState.Error)
    }

    private suspend fun updateProductStatusArrangeAct(inStock: Boolean): Product? {
        val shoppingList = getShoppingList()
        val existingAisle =
            shoppingList.aisles.first { it.products.count { p -> !p.product.inStock } > 0 }

        val aisleProduct = existingAisle.products.first { it.product.inStock == !inStock }
        val productRepository = get<ProductRepository>()
        val existingProduct = productRepository.get(aisleProduct.product.id)!!
        val shoppingListItem =
            getProductShoppingListItemViewModel(existingAisle, aisleProduct, existingProduct)

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateProductStatus(shoppingListItem, inStock)
        return productRepository.get(existingProduct.id)
    }

    @Test
    fun updateProductStatus_InStockTrue_ProductUpdatedToInStock() = runTest {
        val newInStock = true
        val updatedProduct = updateProductStatusArrangeAct(newInStock)
        Assert.assertEquals(newInStock, updatedProduct?.inStock)
    }

    @Test
    fun updateProductStatus_InStockFalse_ProductUpdatedToNotInStock() = runTest {
        val newInStock = false
        val updatedProduct = updateProductStatusArrangeAct(newInStock)
        Assert.assertEquals(newInStock, updatedProduct?.inStock)
    }

    private suspend fun updateAisleExpandedArrangeAct(expanded: Boolean): Aisle? {
        val shoppingList = getShoppingList()
        val existingAisle =
            shoppingList.aisles.first { it.products.count { p -> !p.product.inStock } > 0 }

        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            expanded = existingAisle.expanded,
            isDefault = existingAisle.isDefault,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>(),
            childCount = 5,
            locationId = shoppingList.id
        )

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateAisleExpanded(shoppingListItem, expanded)
        return get<AisleRepository>().get(existingAisle.id)
    }

    @Test
    fun updateAisleExpanded_ExpandedTrue_AisleUpdatedToExpanded() = runTest {
        val newExpanded = true
        val updatedAisle = updateAisleExpandedArrangeAct(newExpanded)
        Assert.assertEquals(newExpanded, updatedAisle?.expanded)
    }

    @Test
    fun updateAisleExpanded_ExpandedFalse_AisleUpdatedToNotExpanded() = runTest {
        val newExpanded = false
        val updatedAisle = updateAisleExpandedArrangeAct(newExpanded)
        Assert.assertEquals(newExpanded, updatedAisle?.expanded)
    }

    @Test
    fun hydrate_AisleCollapsed_AisleItemsHidden() = runTest {
        val domainShoppingList = getShoppingList()

        shoppingListViewModel.hydrate(domainShoppingList.id, domainShoppingList.defaultFilter)
        val shoppingListBefore =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList

        val aisleSummaryBefore =
            shoppingListBefore.groupingBy { it.aisleId }.eachCount().maxBy { it.value }

        val shoppingListItem = shoppingListBefore.first {
            it.itemType == ShoppingListItem.ItemType.AISLE && it.aisleId == aisleSummaryBefore.key
        }

        shoppingListViewModel.updateAisleExpanded(shoppingListItem as AisleShoppingListItem, false)

        shoppingListViewModel.hydrate(domainShoppingList.id, domainShoppingList.defaultFilter)
        val shoppingListAfter =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList

        val aisleCountAfter = shoppingListAfter.count { it.aisleId == aisleSummaryBefore.key }

        assertTrue(aisleSummaryBefore.value > 1)
        assertEquals(1, aisleCountAfter)
    }

    private suspend fun getShoppingList(): Location {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val shoppingList = locationRepo.getLocationWithAislesWithProducts(locationId).first()!!
        return shoppingList
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
    fun requestDefaultList_ShowEmptyAisles_UiStateHasAllAislesAndProducts() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val emptyAisleName = "Empty Aisle"
        get<AisleRepository>().add(
            Aisle(
                name = emptyAisleName,
                locationId = locationId,
                rank = 1001,
                products = emptyList(),
                id = 0,
                isDefault = false,
                expanded = false
            )
        )

        val location = locationRepo.getLocationWithAislesWithProducts(locationId).first()!!
        val aisleCount = location.aisles.count()
        var productCount = 0
        location.aisles.forEach {
            productCount += it.products.count()
        }

        shoppingListViewModel.hydrate(location.id, FilterType.ALL, true)
        shoppingListViewModel.requestDefaultList()

        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList

        Assert.assertNotNull(
            shoppingList.firstOrNull {
                it.itemType == ShoppingListItem.ItemType.AISLE && it.name == emptyAisleName
            }
        )

        Assert.assertEquals(
            aisleCount, shoppingList.count { it.itemType == ShoppingListItem.ItemType.AISLE }
        )

        Assert.assertEquals(
            productCount, shoppingList.count { it.itemType == ShoppingListItem.ItemType.PRODUCT }
        )
    }

    @Test
    fun requestDefaultList_HideEmptyAisles_UiStateHasPopulatedAislesAndAllProducts() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val emptyAisleName = "Empty Aisle"
        get<AisleRepository>().add(
            Aisle(
                name = emptyAisleName,
                locationId = locationId,
                rank = 1001,
                products = emptyList(),
                id = 0,
                isDefault = false,
                expanded = false
            )
        )

        val location = locationRepo.getLocationWithAislesWithProducts(locationId).first()!!
        val aisleCount = location.aisles.count()
        var productCount = 0
        location.aisles.forEach {
            productCount += it.products.count()
        }

        shoppingListViewModel.hydrate(location.id, FilterType.ALL, false)
        shoppingListViewModel.requestDefaultList()

        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList

        Assert.assertNull(
            shoppingList.firstOrNull {
                it.itemType == ShoppingListItem.ItemType.AISLE && it.name == emptyAisleName
            }
        )

        Assert.assertTrue(
            aisleCount > shoppingList.count { it.itemType == ShoppingListItem.ItemType.AISLE }
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
            get<UpdateAisleProductRankUseCase>(),
            get<UpdateAisleRankUseCase>(),
            get<RemoveAisleUseCase>(),
            get<RemoveProductUseCase>(),
            get<GetAisleUseCase>(),
            get<UpdateAisleExpandedUseCase>(),
            get<SortLocationByNameUseCase>(),
            get<GetLoyaltyCardForLocationUseCase>(),
            get<UpdateProductQtyNeededUseCase>(),
            get<ExpandCollapseAislesForLocationUseCase>(),
            get<GetAislesForLocationUseCase>(),
            get<ChangeProductAisleUseCase>(),
        )

        Assert.assertNotNull(vm)
    }

    @Test
    fun removeItem_ExceptionRaised_UiStateIsError() {
        val exceptionMessage = "Error on Remove Item"

        declare<GetAisleUseCase> {
            object : GetAisleUseCase {
                override suspend operator fun invoke(id: Int): Aisle {
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
            expanded = true,
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

        val shoppingListItem = getAisleShoppingListItemViewModel(movedAisle)

        val precedingAisle = aisleRepository.getAll()
            .first { it.locationId == movedAisle.locationId && !it.isDefault && it.id != movedAisle.id }

        val precedingItem = getAisleShoppingListItemViewModel(precedingAisle)

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

        val shoppingListItem = getAisleShoppingListItemViewModel(existingAisle)

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.removeItem(shoppingListItem)

        val removedAisle = aisleRepository.get(existingAisle.id)
        Assert.assertNull(removedAisle)
    }

    private fun defaultAisleTestArrangeAct(showDefaultAisle: Boolean): ShoppingListItem? {
        runBlocking {
            val location = get<LocationRepository>().getShops().first().first().copy(
                id = 0,
                name = "Show Default Aisle $showDefaultAisle",
                showDefaultAisle = showDefaultAisle
            )

            val locationId = get<AddLocationUseCase>().invoke(location)

            shoppingListViewModel.hydrate(locationId, location.defaultFilter)
        }

        return (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated)
            .shoppingList.firstOrNull { it is AisleShoppingListItem && it.isDefault }
    }

    @Test
    fun hydrate_ShopShowsDefaultAisle_DefaultAisleIncluded() = runTest {
        val defaultAisle = defaultAisleTestArrangeAct(true)

        // Assert
        assertNotNull(defaultAisle)
    }

    @Test
    fun hydrate_ShopHidesDefaultAisle_DefaultAisleExcluded() = runTest {
        val defaultAisle = defaultAisleTestArrangeAct(false)

        // Assert
        assertNull(defaultAisle)
    }

    @Test
    fun sortListByName_AisleNameIsAAA_AisleIsRankedFirst() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val aisleRepository = get<AisleRepository>()
        val aisleId = aisleRepository.add(
            Aisle(
                name = "AAA",
                products = emptyList(),
                locationId = existingLocation.id,
                rank = 2001,
                isDefault = false,
                id = 0,
                expanded = true
            )
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        shoppingListViewModel.sortListByName()

        val sortedAisle = aisleRepository.get(aisleId)
        Assert.assertEquals(1, sortedAisle?.rank)
    }

    @Test
    fun sortListByName_ExceptionRaised_UiStateIsError() {
        val exceptionMessage = "Error on Sort by Name"

        declare<SortLocationByNameUseCase> {
            object : SortLocationByNameUseCase {
                override suspend operator fun invoke(locationId: Int) {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<ShoppingListViewModel>()

        vm.hydrate(1, FilterType.NEEDED)
        vm.sortListByName()

        val uiState = vm.shoppingListUiState.value
        Assert.assertTrue(uiState is ShoppingListViewModel.ShoppingListUiState.Error)
        with(uiState as ShoppingListViewModel.ShoppingListUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @Test
    fun hydrate_LocationHasLoyaltyCard_LoyaltyCardPopulated() = runTest {
        val existingLocation =
            get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

        val loyaltyCard = LoyaltyCard(
            id = 0,
            name = "Test Loyalty Card",
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Dummy Intent"
        )

        val loyaltyCardRepository = get<LoyaltyCardRepository>()
        val loyaltyCardId = loyaltyCardRepository.add(loyaltyCard)
        loyaltyCardRepository.addToLocation(existingLocation.id, loyaltyCardId)

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)

        assertEquals(loyaltyCard.copy(id = loyaltyCardId), shoppingListViewModel.loyaltyCard)
    }

    @Test
    fun hydrate_LocationHasNoLoyaltyCard_LoyaltyCardIsNull() = runTest {
        val existingLocation =
            get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

        val loyaltyCardRepository = get<LoyaltyCardRepository>()
        val loyaltyCard = loyaltyCardRepository.getForLocation(existingLocation.id)
        loyaltyCard?.let {
            loyaltyCardRepository.removeFromLocation(existingLocation.id, it.id)
        }

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)

        assertNull(shoppingListViewModel.loyaltyCard)
    }

    @Test
    fun movedItem_ItemWasMoved_ShowAllListItems() = runTest {
        val aisleName = "Empty Aisle"
        val existingLocation =
            get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

        get<AddAisleUseCase>().invoke(
            Aisle(
                name = aisleName,
                products = emptyList(),
                locationId = existingLocation.id,
                rank = 999,
                id = 0,
                isDefault = false,
                expanded = true
            )
        )

        shoppingListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        val shoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList

        val item = shoppingList.first { it.itemType == ShoppingListItem.ItemType.AISLE }

        shoppingListViewModel.movedItem(item)

        val fullShoppingList =
            (shoppingListViewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated).shoppingList

        assertTrue { shoppingList.count() < fullShoppingList.count() }
        assertNull(shoppingList.firstOrNull { it.itemType == ShoppingListItem.ItemType.AISLE && it.name == aisleName })
        assertNotNull(fullShoppingList.firstOrNull { it.itemType == ShoppingListItem.ItemType.AISLE && it.name == aisleName })
    }

    @Test
    fun setShowEmptyAisles_ValueChanged_UiStateIsUpdated() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val showEmptyAisles = false

        shoppingListViewModel.hydrate(locationId, FilterType.ALL, showEmptyAisles)
        shoppingListViewModel.clearState()
        val uiStateBefore = shoppingListViewModel.shoppingListUiState.value

        shoppingListViewModel.setShowEmptyAisles(!showEmptyAisles)

        val uiStateAfter = shoppingListViewModel.shoppingListUiState.value

        assertNotEquals(uiStateBefore, uiStateAfter)
    }

    @Test
    fun setShowEmptyAisles_ValueUnchanged_UiStateIsNotUpdated() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val showEmptyAisles = false

        shoppingListViewModel.hydrate(locationId, FilterType.ALL, showEmptyAisles)
        shoppingListViewModel.clearState()
        val uiStateBefore = shoppingListViewModel.shoppingListUiState.value

        shoppingListViewModel.setShowEmptyAisles(showEmptyAisles)

        val uiStateAfter = shoppingListViewModel.shoppingListUiState.value

        assertEquals(uiStateBefore, uiStateAfter)
    }

    private suspend fun updateProductNeededQuantityArrangeAct(
        qtyInitial: Double, qtyNew: Double?
    ): Int {
        val shoppingList = getShoppingList()
        val existingAisle =
            shoppingList.aisles.first { it.products.count { p -> !p.product.inStock } > 0 }

        val aisleProduct = existingAisle.products.first()
        val productRepository = get<ProductRepository>()
        productRepository.update(aisleProduct.product.copy(qtyNeeded = qtyInitial))
        val existingProduct = productRepository.get(aisleProduct.product.id)!!
        val shoppingListItem =
            getProductShoppingListItemViewModel(existingAisle, aisleProduct, existingProduct)

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateProductNeededQuantity(shoppingListItem, qtyNew)
        return existingProduct.id
    }

    @Test
    fun updateProductNeededQuantity_ValidQty_ProductQtyNeededUpdated() = runTest {
        val qtyInitial = 5.0
        val qtyNew = 10.0
        val productId = updateProductNeededQuantityArrangeAct(qtyInitial, qtyNew)
        val updatedProduct = get<ProductRepository>().get(productId)
        Assert.assertEquals(qtyNew, updatedProduct?.qtyNeeded)
    }

    @Test
    fun updateProductNeededQuantity_NegativeQty_UiStateIsError() = runTest {
        val qtyInitial = 5.0
        val qtyNew = -1.0
        updateProductNeededQuantityArrangeAct(qtyInitial, qtyNew)
        Assert.assertTrue(shoppingListViewModel.shoppingListUiState.value is ShoppingListViewModel.ShoppingListUiState.Error)
    }

    @Test
    fun updateProductNeededQuantity_QtyIsNull_QtyNotUpdated() = runTest {
        val qtyInitial = 5.0
        val qtyNew = null
        val productId = updateProductNeededQuantityArrangeAct(qtyInitial, qtyNew)
        val updatedProduct = get<ProductRepository>().get(productId)
        Assert.assertEquals(qtyInitial, updatedProduct?.qtyNeeded)
    }

    @Test
    fun expandCollapseAisles_HasAisles_ExpandedCountChanges() = runTest {
        val locationId =
            get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }.id

        val aisleRepository = get<AisleRepository>()
        val expandedBefore = aisleRepository.getAll().count { it.expanded }
        shoppingListViewModel.hydrate(locationId, FilterType.ALL)

        shoppingListViewModel.expandCollapseAisles()
        val expandedAfter = aisleRepository.getAll().count { it.expanded }

        assertTrue(expandedBefore > expandedAfter)
    }

    @Test
    fun selectedItem_SetAndClear_HandledCorrectly() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        shoppingListViewModel.hydrate(aisle.locationId, FilterType.ALL)
        val shoppingListItem = getAisleShoppingListItemViewModel(aisle)

        shoppingListViewModel.setSelectedItem(shoppingListItem)

        assertEquals(shoppingListItem, shoppingListViewModel.selectedItem)

        shoppingListViewModel.clearSelectedItem()

        assertNull(shoppingListViewModel.selectedItem)
    }

    @Test
    fun updateSelectedProductAisle_ItemIsProduct_ProductAisleUpdated() = runTest {
        val shoppingList = getShoppingList()
        val existingAisle =
            shoppingList.aisles.first { it.products.count { p -> !p.product.inStock } > 0 }

        val aisleProduct = existingAisle.products.first()
        val existingProduct = aisleProduct.product
        val shoppingListItem =
            getProductShoppingListItemViewModel(existingAisle, aisleProduct, existingProduct)

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.setSelectedItem(shoppingListItem)

        val newAisle = shoppingList.aisles.first { it.id != existingAisle.id }
        shoppingListViewModel.updateSelectedProductAisle(newAisle.id)

        val updatedAisleProduct = get<AisleProductRepository>().get(aisleProduct.id)
        assertEquals(newAisle.id, updatedAisleProduct?.aisleId)
    }

    private fun getProductShoppingListItemViewModel(
        aisle: Aisle,
        aisleProduct: AisleProduct,
        product: Product
    ): ProductShoppingListItemViewModel = ProductShoppingListItemViewModel(
        aisleRank = aisle.rank,
        rank = aisleProduct.rank,
        id = product.id,
        name = product.name,
        inStock = product.inStock,
        qtyNeeded = product.qtyNeeded,
        noteId = null,
        aisleId = aisle.id,
        aisleProductId = aisleProduct.id,
        updateAisleProductRankUseCase = get<UpdateAisleProductRankUseCase>(),
        removeProductUseCase = get<RemoveProductUseCase>()
    )

    private fun getAisleShoppingListItemViewModel(aisle: Aisle): AisleShoppingListItemViewModel =
        AisleShoppingListItemViewModel(
            rank = aisle.rank,
            id = aisle.id,
            name = aisle.name,
            isDefault = aisle.isDefault,
            childCount = 0,
            locationId = aisle.locationId,
            expanded = aisle.expanded,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

    @Test
    fun updateSelectedProductAisle_ItemIsAisle_NoActionTaken() = runTest {
        var changeAisleCalled = false
        declare<ChangeProductAisleUseCase> {
            object : ChangeProductAisleUseCase {
                override suspend fun invoke(
                    productId: Int, currentAisleId: Int, newAisleId: Int
                ) {
                    changeAisleCalled = true
                }
            }
        }

        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        shoppingListViewModel.hydrate(aisle.locationId, FilterType.ALL)
        val shoppingListItem = getAisleShoppingListItemViewModel(aisle)

        shoppingListViewModel.hydrate(aisle.id, FilterType.ALL)
        shoppingListViewModel.setSelectedItem(shoppingListItem)

        shoppingListViewModel.updateSelectedProductAisle(aisle.id)

        assertFalse(changeAisleCalled)
    }

    @Test
    fun requestLocationAisles_ValidProduct_EmitsAislePickerBundle() = runTest {
        // Arrange
        val shoppingList = getShoppingList()
        val existingAisle = shoppingList.aisles.first()
        val aisleProduct = existingAisle.products.first()
        val product = get<ProductRepository>().get(aisleProduct.product.id)!!
        val shoppingListItem =
            getProductShoppingListItemViewModel(existingAisle, aisleProduct, product)

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)

        // Act
        shoppingListViewModel.requestLocationAisles(shoppingListItem)

        // Assert
        val aislesForLocation = shoppingListViewModel.aislesForLocation.first { it != null }
        assertNotNull(aislesForLocation)
        assertEquals(product.name, aislesForLocation.title)
        assertEquals(aisleProduct.aisleId, aislesForLocation.currentAisleId)
        assertTrue(aislesForLocation.aisles.isNotEmpty())

        // Verify aisles are sorted by rank
        val sortedAisles = shoppingList.aisles.sortedBy { it.rank }
        assertEquals(sortedAisles.size, aislesForLocation.aisles.size)
        sortedAisles.forEachIndexed { index, aisle ->
            assertEquals(aisle.id, aislesForLocation.aisles[index].id)
            assertEquals(aisle.name, aislesForLocation.aisles[index].name)
        }
    }

    @Test
    fun updateSelectedProductAisle_AisleIsFromDifferentLocation_UiStateIsAisleronError() = runTest {
        val shoppingList = getShoppingList()
        val existingAisle = shoppingList.aisles.first()
        val aisleProduct = existingAisle.products.first()
        val product = aisleProduct.product
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)

        val shoppingListItem =
            getProductShoppingListItemViewModel(existingAisle, aisleProduct, product)

        shoppingListViewModel.setSelectedItem(shoppingListItem)

        val changeToAisle =
            get<AisleRepository>().getAll().first { it.locationId != existingAisle.locationId }

        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.updateSelectedProductAisle(changeToAisle.id)

        val result =
            shoppingListViewModel.shoppingListUiState.value as? ShoppingListViewModel.ShoppingListUiState.Error

        Assert.assertEquals(AisleronException.ExceptionCode.AISLE_MOVE_EXCEPTION, result?.errorCode)
    }
}