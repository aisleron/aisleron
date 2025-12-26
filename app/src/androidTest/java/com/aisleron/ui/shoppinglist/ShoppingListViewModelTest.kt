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

        val shoppingList = uiStateAsUpdated(shoppingListViewModel).shoppingList

        assertEquals(1, shoppingList.count())
        assertEquals(1, shoppingList.count { it.itemType == ShoppingListItem.ItemType.EMPTY_LIST })
    }

    @Test
    fun hydrate_ListHasAislesAndProducts_EmptyListItemExcluded() = runTest {
        val location = getShoppingList()
        shoppingListViewModel.hydrate(location.id, location.defaultFilter)

        val shoppingList = uiStateAsUpdated(shoppingListViewModel).shoppingList

        assertTrue(shoppingList.isNotEmpty())
        assertEquals(0, shoppingList.count { it.itemType == ShoppingListItem.ItemType.EMPTY_LIST })
    }

    private fun uiStateAsUpdated(viewModel: ShoppingListViewModel): ShoppingListViewModel.ShoppingListUiState.Updated {
        return viewModel.shoppingListUiState.value as ShoppingListViewModel.ShoppingListUiState.Updated
    }

    @Test
    fun removeItem_SelectedItemsIsDefaultAisle_UiStateIsError() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.setShowEmptyAisles(true)
        val existingAisle = getAisleListItems(shoppingListViewModel).first { it.isDefault }
        shoppingListViewModel.toggleItemSelection(existingAisle)

        shoppingListViewModel.removeSelectedItems()

        assertNotNull(uiStateAsError(shoppingListViewModel))
    }

    private suspend fun updateProductStatusArrangeAct(newInStock: Boolean): Product? {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, FilterType.ALL)
        val shoppingListItem =
            getProductListItems(shoppingListViewModel).first { it.inStock == newInStock }

        shoppingListViewModel.updateProductStatus(shoppingListItem, newInStock)
        return get<ProductRepository>().get(shoppingListItem.id)
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
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        val shoppingListItem = getAisleListItems(shoppingListViewModel).first()

        shoppingListViewModel.updateAisleExpanded(shoppingListItem, expanded)

        return get<AisleRepository>().get(shoppingListItem.aisleId)
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
        val shoppingListBefore = uiStateAsUpdated(shoppingListViewModel).shoppingList

        val aisleSummaryBefore =
            shoppingListBefore.groupingBy { it.aisleId }.eachCount().maxBy { it.value }

        val shoppingListItem = shoppingListBefore.filterIsInstance<AisleShoppingListItem>().first {
            it.aisleId == aisleSummaryBefore.key
        }

        shoppingListViewModel.updateAisleExpanded(shoppingListItem, false)

        assertTrue(aisleSummaryBefore.value > 1)

        // Create a new instance of the viewmodel to verify results because hydrate won't run again.
        val vm = get<ShoppingListViewModel>()
        vm.hydrate(domainShoppingList.id, domainShoppingList.defaultFilter)
        val shoppingListAfter = uiStateAsUpdated(vm).shoppingList
        val aisleCountAfter = shoppingListAfter.count { it.aisleId == aisleSummaryBefore.key }
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

        val shoppingList = uiStateAsUpdated(shoppingListViewModel).shoppingList
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

        val shoppingList = uiStateAsUpdated(shoppingListViewModel).shoppingList
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

        val shoppingList = uiStateAsUpdated(shoppingListViewModel).shoppingList

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

        val shoppingList = uiStateAsUpdated(shoppingListViewModel).shoppingList

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
    fun removeSelectedItems_ExceptionRaised_UiStateIsError() {
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
        val sli = getAisleListItems(vm).first()
        vm.toggleItemSelection(sli)

        vm.removeSelectedItems()

        val uiState = uiStateAsError(vm)
        assertNotNull(uiState)
        assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, uiState.errorCode)
        assertEquals(exceptionMessage, uiState.errorMessage)
    }

    @Test
    fun updateItemRank_ItemIsAisle_AisleRankUpdated() = runTest {
        val shoppingList = getShoppingList()
        val movedAisle = shoppingList.aisles.last { !it.isDefault }
        val precedingAisle = shoppingList.aisles.first { !it.isDefault && it.id != movedAisle.id }
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.setShowEmptyAisles(true)

        val shoppingListItem =
            getAisleListItems(shoppingListViewModel).first { it.id == movedAisle.id }

        val precedingItem =
            getAisleListItems(shoppingListViewModel).first { it.id == precedingAisle.id }

        shoppingListViewModel.updateItemRank(shoppingListItem, precedingItem)

        val updatedAisle = get<AisleRepository>().get(movedAisle.id)
        Assert.assertEquals(precedingItem.rank + 1, updatedAisle?.rank)
    }

    @Test
    fun removeItem_SelectedItemsIsStandardAisle_AisleRemoved() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.setShowEmptyAisles(true)
        val shoppingListItem = getAisleListItems(shoppingListViewModel).first { !it.isDefault }
        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        shoppingListViewModel.removeSelectedItems()

        val removedAisle = get<AisleRepository>().get(shoppingListItem.id)
        Assert.assertNull(removedAisle)
    }

    private suspend fun defaultAisleTestArrangeAct(showDefaultAisle: Boolean): ShoppingListItem? {
        val location = get<LocationRepository>().getShops().first().first().copy(
            id = 0,
            name = "Show Default Aisle $showDefaultAisle",
            showDefaultAisle = showDefaultAisle
        )

        val locationId = get<AddLocationUseCase>().invoke(location)
        shoppingListViewModel.hydrate(locationId, location.defaultFilter)

        return getAisleListItems(shoppingListViewModel).firstOrNull { it.isDefault }
    }

    @Test
    fun hydrate_ShopShowsDefaultAisle_DefaultAisleIncluded() = runTest {
        val defaultAisle = defaultAisleTestArrangeAct(true)
        assertNotNull(defaultAisle)
    }

    @Test
    fun hydrate_ShopHidesDefaultAisle_DefaultAisleExcluded() = runTest {
        val defaultAisle = defaultAisleTestArrangeAct(false)
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

        val uiState = uiStateAsError(vm)
        assertNotNull(uiState)
        Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, uiState.errorCode)
        Assert.assertEquals(exceptionMessage, uiState.errorMessage)
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
        val shoppingList = uiStateAsUpdated(shoppingListViewModel).shoppingList

        val item = shoppingList.first { it.itemType == ShoppingListItem.ItemType.AISLE }

        shoppingListViewModel.movedItem(item)

        val fullShoppingList = uiStateAsUpdated(shoppingListViewModel).shoppingList

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

        val existingProduct = existingAisle.products.first { !it.product.inStock }.product
        get<ProductRepository>().update(existingProduct.copy(qtyNeeded = qtyInitial))
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        val shoppingListItem =
            getProductListItems(shoppingListViewModel).first { it.id == existingProduct.id }

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
    fun selectedItems_SetAndClear_HandledCorrectly() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        shoppingListViewModel.hydrate(aisle.locationId, FilterType.ALL)
        val shoppingListItem = getAisleListItems(shoppingListViewModel).first { it.id == aisle.id }

        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        assertEquals(
            shoppingListItem.copyWith(selected = true),
            shoppingListViewModel.selectedListItems.first()
        )

        shoppingListViewModel.clearSelectedListItems()
        assertNull(shoppingListViewModel.selectedListItems.firstOrNull())
    }

    @Test
    fun updateSelectedProductAisle_ItemIsProduct_ProductAisleUpdated() = runTest {
        val shoppingList = getShoppingList()
        val existingAisle =
            shoppingList.aisles.first { it.products.count { p -> !p.product.inStock } > 0 }

        val aisleProduct = existingAisle.products.first { !it.product.inStock }
        val existingProduct = aisleProduct.product
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        val shoppingListItem =
            getProductListItems(shoppingListViewModel).first { it.id == existingProduct.id }

        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        val newAisle = shoppingList.aisles.first { it.id != existingAisle.id }
        shoppingListViewModel.updateSelectedProductAisle(newAisle.id)

        val updatedAisleProduct = get<AisleProductRepository>().get(aisleProduct.id)
        assertEquals(newAisle.id, updatedAisleProduct?.aisleId)
    }

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
        val shoppingListItem = getAisleListItems(shoppingListViewModel).first { it.id == aisle.id }
        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        shoppingListViewModel.updateSelectedProductAisle(aisle.id)

        assertFalse(changeAisleCalled)
    }

    @Test
    fun requestLocationAisles_ValidLocation_EmitsAisleList() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)

        shoppingListViewModel.requestLocationAisles()

        val aislesForLocation = shoppingListViewModel.aislesForLocation.first()
        assertTrue(aislesForLocation.isNotEmpty())

        // Verify aisles are sorted by rank
        val sortedAisles = shoppingList.aisles.sortedBy { it.rank }
        assertEquals(sortedAisles.size, aislesForLocation.size)
        sortedAisles.forEachIndexed { index, aisle ->
            assertEquals(aisle.id, aislesForLocation[index].id)
            assertEquals(aisle.name, aislesForLocation[index].name)
        }
    }

    @Test
    fun clearLocationAisles_LocationAislesListCleared() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        shoppingListViewModel.requestLocationAisles()

        // Validate that aisles actually exist
        val aislesForLocation = shoppingListViewModel.aislesForLocation.first()
        assertTrue(aislesForLocation.isNotEmpty())

        shoppingListViewModel.clearLocationAisles()

        val aislesForLocationAfterClear = shoppingListViewModel.aislesForLocation.first()
        assertTrue(aislesForLocationAfterClear.isEmpty())
    }

    private fun uiStateAsError(viewModel: ShoppingListViewModel): ShoppingListViewModel.ShoppingListUiState.Error? {
        return viewModel.shoppingListUiState.value as? ShoppingListViewModel.ShoppingListUiState.Error
    }

    @Test
    fun updateSelectedProductAisle_AisleIsFromDifferentLocation_UiStateIsAisleronError() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        val shoppingListItem = getProductListItems(shoppingListViewModel).first()

        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        val changeToAisle =
            get<AisleRepository>().getAll().first { it.locationId != shoppingList.id }

        shoppingListViewModel.updateSelectedProductAisle(changeToAisle.id)

        val result = uiStateAsError(shoppingListViewModel)
        Assert.assertEquals(AisleronException.ExceptionCode.AISLE_MOVE_EXCEPTION, result?.errorCode)
    }

    @Test
    fun getSelectedItemAisleId_SingleSelectedItem_AisleIdReturned() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        val shoppingListItem = getProductListItems(shoppingListViewModel).first()

        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        val aisleId = shoppingListViewModel.getSelectedItemAisleId()

        assertEquals(shoppingListItem.aisleId, aisleId)
    }

    @Test
    fun getSelectedItemAisleId_NoSelectedItems_ReturnMinus1() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)

        val aisleId = shoppingListViewModel.getSelectedItemAisleId()

        assertEquals(-1, aisleId)
    }

    @Test
    fun getSelectedItemAisleId_MultipleSelectedItems_ReturnMinus1() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)
        val item1 = getProductListItems(shoppingListViewModel).first()
            .also { shoppingListViewModel.toggleItemSelection(it) }

        getProductListItems(shoppingListViewModel).first { it.id != item1.id }
            .also { shoppingListViewModel.toggleItemSelection(it) }

        val aisleId = shoppingListViewModel.getSelectedItemAisleId()

        assertEquals(-1, aisleId)
    }

    private fun getAisleListItems(viewModel: ShoppingListViewModel): List<AisleShoppingListItem> =
        uiStateAsUpdated(viewModel).shoppingList.filterIsInstance<AisleShoppingListItem>()


    private fun getProductListItems(viewModel: ShoppingListViewModel): List<ProductShoppingListItem> =
        uiStateAsUpdated(viewModel).shoppingList.filterIsInstance<ProductShoppingListItem>()

    @Test
    fun removeSelectedItems_MultipleItemsSelected_RemoveAllItems() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)

        val aisleItem = getAisleListItems(shoppingListViewModel).first { !it.isDefault }
            .also { shoppingListViewModel.toggleItemSelection(it) }

        val productItem = getProductListItems(shoppingListViewModel).first()
            .also { shoppingListViewModel.toggleItemSelection(it) }

        // Validate that the aisle and product exist in the repository
        assertNotNull(get<AisleRepository>().get(aisleItem.id))
        assertNotNull(get<ProductRepository>().get(productItem.id))

        shoppingListViewModel.removeSelectedItems()

        val removedAisle = get<AisleRepository>().get(aisleItem.id)
        assertNull(removedAisle)

        val removedProduct = get<ProductRepository>().get(productItem.id)
        assertNull(removedProduct)
    }

    @Test
    fun updateSelectedProductAisle_MultipleItemsSelected_UpdateAllItems() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(shoppingList.id, shoppingList.defaultFilter)

        val productOne = getProductListItems(shoppingListViewModel).first()
            .also { shoppingListViewModel.toggleItemSelection(it) }

        val productTwo = getProductListItems(shoppingListViewModel).first { it.id != productOne.id }
            .also { shoppingListViewModel.toggleItemSelection(it) }

        // Validate that the products exist in the repository
        val productRepository = get<ProductRepository>()
        assertNotNull(productRepository.get(productOne.id))
        assertNotNull(productRepository.get(productTwo.id))

        val newAisle = get<AisleRepository>().getAll()
            .first { it.locationId == shoppingList.id && it.id != productOne.aisleId && it.id != productTwo.aisleId }

        shoppingListViewModel.updateSelectedProductAisle(newAisle.id)

        val aisleProductRepository = get<AisleProductRepository>()
        val updatedProductOne = aisleProductRepository.getProductAisles(productOne.id)
            .singleOrNull { it.aisleId == newAisle.id }

        assertNotNull(updatedProductOne)

        val updatedProductTwo = aisleProductRepository.getProductAisles(productTwo.id)
            .singleOrNull { it.aisleId == newAisle.id }

        assertNotNull(updatedProductTwo)
    }
}