/*
 * Copyright (C) 2025-2026 aisleron.com
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
import com.aisleron.di.factoryModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.SortLocationByNameUseCase
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.domain.shoppinglist.ShoppingListFilter
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import com.aisleron.ui.shoppinglist.coordinator.ShoppingListCoordinatorFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
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

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModelTest : KoinTest {
    private lateinit var shoppingListViewModel: ShoppingListViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule, viewModelTestModule, repositoryModule, useCaseModule, factoryModule
        )
    )

    @Before
    fun setUp() {
        shoppingListViewModel = get<ShoppingListViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    private fun getAisleGrouping(locationId: Int): ShoppingListGrouping.AisleGrouping =
        ShoppingListGrouping.AisleGrouping(locationId)

    @Test
    fun hydrate_IsValidLocation_LocationMembersAreCorrect() = runTest {
        val existingLocation =
            get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

        shoppingListViewModel.hydrate(
            getAisleGrouping(existingLocation.id),
            existingLocation.defaultFilter
        )

        val result = awaitUiStateUpdated(shoppingListViewModel)
        assertEquals(existingLocation.type, result.locationType)
        assertEquals(existingLocation.defaultFilter, shoppingListViewModel.productFilter)
        assertEquals(
            existingLocation.name,
            (result.title as ShoppingListViewModel.ListTitle.LocationName).name
        )
    }

    @Test
    fun hydrate_IsInvalidLocation_LocationMembersAreDefault() = runTest {
        shoppingListViewModel.hydrate(getAisleGrouping(-1), FilterType.NEEDED)

        val result = awaitUiStateUpdated(shoppingListViewModel)

        Assert.assertEquals(ShoppingListViewModel.ListTitle.Needed, result.title)
        Assert.assertEquals(LocationType.HOME, result.locationType)
    }

    @Test
    fun hydrate_LocationHasNoAislesOrProducts_EmptyListItemAdded() = runTest {
        val location = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "No Aisle Shop",
            pinned = false,
            aisles = emptyList(),
            showDefaultAisle = false,
            expanded = true,
            rank = get<LocationRepository>().getMaxRank() + 1
        )

        val locationId = get<LocationRepository>().add(location)
        shoppingListViewModel.hydrate(getAisleGrouping(locationId), location.defaultFilter)

        val shoppingList = awaitUiStateUpdated(shoppingListViewModel).shoppingList

        assertEquals(1, shoppingList.count())
        assertEquals(1, shoppingList.count { it.itemType == ShoppingListItem.ItemType.EMPTY_LIST })
    }

    @Test
    fun hydrate_ListHasAislesAndProducts_EmptyListItemExcluded() = runTest {
        val location = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(location.id), location.defaultFilter)

        val shoppingList = awaitUiStateUpdated(shoppingListViewModel).shoppingList

        assertTrue(shoppingList.isNotEmpty())
        assertEquals(0, shoppingList.count { it.itemType == ShoppingListItem.ItemType.EMPTY_LIST })
    }

    private suspend fun awaitUiStateUpdated(
        viewModel: ShoppingListViewModel
    ): ShoppingListViewModel.ShoppingListUiState.Updated {
        // We use first() to wait until the Flow emits a state that is Updated
        return viewModel.shoppingListUiState
            .first { it is ShoppingListViewModel.ShoppingListUiState.Updated }
                as ShoppingListViewModel.ShoppingListUiState.Updated
    }

    private suspend fun awaitLoyaltyCard(viewModel: ShoppingListViewModel): LoyaltyCard? {
        return viewModel.loyaltyCard.first()
    }

    @Test
    fun removeItem_SelectedItemsIsDefaultAisle_UiStateIsError() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        shoppingListViewModel.setShowEmptyAisles(true)
        val existingAisle = getAisleListItems(shoppingListViewModel).first { it.isDefault }
        shoppingListViewModel.toggleItemSelection(existingAisle)

        val event = awaitEvent(shoppingListViewModel) {
            shoppingListViewModel.removeSelectedItems()
        }

        assert(event is ShoppingListViewModel.ShoppingListEvent.ShowError)
    }

    private suspend fun updateProductStatusArrangeAct(newInStock: Boolean): Product? {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), FilterType.ALL)
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

    private suspend fun updateExpandedArrangeAct(expanded: Boolean): Aisle? {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val shoppingListItem = getAisleListItems(shoppingListViewModel).first()

        shoppingListViewModel.updateExpanded(shoppingListItem, expanded)

        return get<AisleRepository>().get(shoppingListItem.aisleId)
    }

    @Test
    fun updateExpanded_ExpandedTrue_UpdatedToExpanded() = runTest {
        val newExpanded = true
        val updatedAisle = updateExpandedArrangeAct(newExpanded)
        Assert.assertEquals(newExpanded, updatedAisle?.expanded)
    }

    @Test
    fun updateExpanded_ExpandedFalse_UpdatedToNotExpanded() = runTest {
        val newExpanded = false
        val updatedAisle = updateExpandedArrangeAct(newExpanded)
        Assert.assertEquals(newExpanded, updatedAisle?.expanded)
    }

    @Test
    fun hydrate_AisleCollapsed_AisleItemsHidden() = runTest {
        val domainShoppingList = getShoppingList()
        shoppingListViewModel.hydrate(
            getAisleGrouping(domainShoppingList.id),
            domainShoppingList.defaultFilter
        )

        val shoppingListBefore = awaitUiStateUpdated(shoppingListViewModel).shoppingList

        val aisleSummaryBefore =
            shoppingListBefore.groupingBy { it.aisleId }.eachCount().maxBy { it.value }

        val shoppingListItem = shoppingListBefore.filterIsInstance<AisleShoppingListItem>().first {
            it.aisleId == aisleSummaryBefore.key
        }

        shoppingListViewModel.updateExpanded(shoppingListItem, false)

        assertTrue(aisleSummaryBefore.value > 1)

        // Create a new instance of the viewmodel to verify results because hydrate won't run again.
        val vm = get<ShoppingListViewModel>()
        vm.hydrate(getAisleGrouping(domainShoppingList.id), domainShoppingList.defaultFilter)
        val shoppingListAfter = awaitUiStateUpdated(vm).shoppingList
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

        shoppingListViewModel.hydrate(
            getAisleGrouping(existingLocation.id),
            existingLocation.defaultFilter
        )

        shoppingListViewModel.submitProductSearch(searchString)

        val shoppingList = awaitUiStateUpdated(shoppingListViewModel).shoppingList
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

        shoppingListViewModel.hydrate(
            getAisleGrouping(existingLocation.id),
            existingLocation.defaultFilter
        )

        shoppingListViewModel.submitProductSearch(searchString)

        val shoppingList = awaitUiStateUpdated(shoppingListViewModel).shoppingList
        Assert.assertEquals(
            productSearchCount,
            shoppingList.count { p -> p.name.contains(searchString) && p.itemType == ShoppingListItem.ItemType.PRODUCT }
        )
    }

    @Test
    fun requestListRefresh_returnDefaultListIsFalse_DoNotUpdateFilters() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id

        shoppingListViewModel.hydrate(getAisleGrouping(locationId), FilterType.ALL, false)

        val aisleCount = getAisleListItems(shoppingListViewModel).count()
        val productCount = getProductListItems(shoppingListViewModel).count()

        shoppingListViewModel.submitProductSearch("Ap")

        val searchedAisleCount = getAisleListItems(shoppingListViewModel).count()
        assertTrue(aisleCount > searchedAisleCount)

        val searchedProductCount = getProductListItems(shoppingListViewModel).count()
        assertTrue(productCount > searchedProductCount)

        shoppingListViewModel.requestListRefresh(false)

        val refreshedAisleCount = getAisleListItems(shoppingListViewModel).count()
        assertEquals(searchedAisleCount, refreshedAisleCount)

        val refreshedProductCount = getProductListItems(shoppingListViewModel).count()
        assertEquals(searchedProductCount, refreshedProductCount)
    }

    @Test
    fun requestListRefresh_returnDefaultListIsTrue_ReturnDefaultList() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id

        shoppingListViewModel.hydrate(getAisleGrouping(locationId), FilterType.ALL, false)

        val aisleCount = getAisleListItems(shoppingListViewModel).count()
        val productCount = getProductListItems(shoppingListViewModel).count()

        shoppingListViewModel.submitProductSearch("Ap")

        val searchedAisleCount = getAisleListItems(shoppingListViewModel).count()
        assertTrue(aisleCount > searchedAisleCount)

        val searchedProductCount = getProductListItems(shoppingListViewModel).count()
        assertTrue(productCount > searchedProductCount)

        shoppingListViewModel.requestListRefresh(true)

        val refreshedAisleCount = getAisleListItems(shoppingListViewModel).count()
        assertEquals(aisleCount, refreshedAisleCount)

        val refreshedProductCount = getProductListItems(shoppingListViewModel).count()
        assertEquals(productCount, refreshedProductCount)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_ShoppingListViewModelReturned() {
        val vm = ShoppingListViewModel(
            shoppingListStreamProviderFactory = get<ShoppingListCoordinatorFactory>()
        )

        Assert.assertNotNull(vm)
    }

    private suspend fun TestScope.awaitEvent(
        viewModel: ShoppingListViewModel,
        trigger: suspend () -> Unit
    ): ShoppingListViewModel.ShoppingListEvent {
        // We create a "trap" for the event before we trigger the action
        val events = viewModel.events

        // Start collecting in the background
        val deferred = CompletableDeferred<ShoppingListViewModel.ShoppingListEvent>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            events.collect { deferred.complete(it) }
        }

        // Trigger the action (e.g., removeSelectedItems)
        trigger()

        // Wait for the trap to snap shut
        return withTimeout(2000) {
            val result = deferred.await()
            job.cancel()
            result
        }
    }

    @Test
    fun removeSelectedItems_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on Remove Item"

        declare<GetAisleUseCase> {
            object : GetAisleUseCase {
                override suspend operator fun invoke(id: Int): Aisle {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<ShoppingListViewModel>()
        vm.hydrate(getAisleGrouping(1), FilterType.NEEDED)
        val sli = getAisleListItems(vm).first()
        vm.toggleItemSelection(sli)

        val event = awaitEvent(vm) {
            vm.removeSelectedItems()
        }

        assert(event is ShoppingListViewModel.ShoppingListEvent.ShowError)

        val error = event as ShoppingListViewModel.ShoppingListEvent.ShowError
        assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, error.errorCode)
        assertEquals(exceptionMessage, error.errorMessage)
    }

    @Test
    fun updateItemRank_ItemIsAisle_AisleRankUpdated() = runTest {
        val shoppingList = getShoppingList()
        val movedAisle = shoppingList.aisles.last { !it.isDefault }
        val precedingAisle = shoppingList.aisles.first { !it.isDefault && it.id != movedAisle.id }
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
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
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
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
        shoppingListViewModel.hydrate(getAisleGrouping(locationId), location.defaultFilter)

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

        shoppingListViewModel.hydrate(
            getAisleGrouping(existingLocation.id),
            existingLocation.defaultFilter
        )

        shoppingListViewModel.sortListByName()

        val sortedAisle = aisleRepository.get(aisleId)
        Assert.assertEquals(1, sortedAisle?.rank)
    }

    @Test
    fun sortListByName_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on Sort by Name"
        declare<SortLocationByNameUseCase> {
            object : SortLocationByNameUseCase {
                override suspend operator fun invoke(locationId: Int) {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<ShoppingListViewModel>()
        vm.hydrate(getAisleGrouping(1), FilterType.NEEDED)

        val event = awaitEvent(vm) {
            vm.sortListByName()
        }

        assert(event is ShoppingListViewModel.ShoppingListEvent.ShowError)

        val error = event as ShoppingListViewModel.ShoppingListEvent.ShowError
        Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, error.errorCode)
        Assert.assertEquals(exceptionMessage, error.errorMessage)
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

        shoppingListViewModel.hydrate(
            getAisleGrouping(existingLocation.id),
            existingLocation.defaultFilter
        )

        val loyaltyCardResult = awaitLoyaltyCard(shoppingListViewModel)
        assertEquals(loyaltyCard.copy(id = loyaltyCardId), loyaltyCardResult)
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

        shoppingListViewModel.hydrate(
            getAisleGrouping(existingLocation.id),
            existingLocation.defaultFilter
        )

        val loyaltyCardResult = awaitLoyaltyCard(shoppingListViewModel)
        assertNull(loyaltyCardResult)
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

        shoppingListViewModel.hydrate(
            getAisleGrouping(existingLocation.id),
            existingLocation.defaultFilter
        )

        val shoppingList = awaitUiStateUpdated(shoppingListViewModel).shoppingList

        val item = shoppingList.first { it.itemType == ShoppingListItem.ItemType.HEADER }

        shoppingListViewModel.movedItem(item)

        val fullShoppingList = awaitUiStateUpdated(shoppingListViewModel).shoppingList

        assertTrue { shoppingList.count() < fullShoppingList.count() }
        assertNull(shoppingList.firstOrNull { it.itemType == ShoppingListItem.ItemType.HEADER && it.name == aisleName })
        assertNotNull(fullShoppingList.firstOrNull { it.itemType == ShoppingListItem.ItemType.HEADER && it.name == aisleName })
    }

    @Test
    fun setShowEmptyAisles_ValueChanged_UiStateIsUpdated() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val showEmptyAisles = false

        shoppingListViewModel.hydrate(getAisleGrouping(locationId), FilterType.ALL, showEmptyAisles)
        val aisleCount = getAisleListItems(shoppingListViewModel).count()
        val uiStateBefore = shoppingListViewModel.shoppingListUiState.value

        shoppingListViewModel.setShowEmptyAisles(!showEmptyAisles)

        val uiStateAfter = shoppingListViewModel.shoppingListUiState.value
        assertNotEquals(uiStateBefore, uiStateAfter)

        val aisleCountAfter = getAisleListItems(shoppingListViewModel).count()
        assertTrue(aisleCount < aisleCountAfter)
    }

    @Test
    fun setShowEmptyAisles_ValueUnchanged_UiStateIsNotUpdated() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val showEmptyAisles = false

        shoppingListViewModel.hydrate(getAisleGrouping(locationId), FilterType.ALL, showEmptyAisles)
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
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
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

        val event = awaitEvent(shoppingListViewModel) {
            updateProductNeededQuantityArrangeAct(qtyInitial, qtyNew)
        }

        assert(event is ShoppingListViewModel.ShoppingListEvent.ShowError)
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
    fun expandCollapseHeaders_HeadersAreAisles_AisleExpandedCountChanges() = runTest {
        val locationId =
            get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }.id

        val aisleRepository = get<AisleRepository>()
        val expandedBefore = aisleRepository.getAll().count { it.expanded }
        shoppingListViewModel.hydrate(getAisleGrouping(locationId), FilterType.ALL)

        shoppingListViewModel.expandCollapseHeaders()
        val expandedAfter = aisleRepository.getAll().count { it.expanded }

        assertTrue(expandedBefore > expandedAfter)
    }

    @Test
    fun selectedItems_SetAndClear_HandledCorrectly() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        shoppingListViewModel.hydrate(getAisleGrouping(aisle.locationId), FilterType.ALL)
        val sliBefore = getAisleListItems(shoppingListViewModel).first { it.id == aisle.id }

        shoppingListViewModel.toggleItemSelection(sliBefore)

        val sliAfter = getAisleListItems(shoppingListViewModel).first { it.id == aisle.id }
        assertEquals(sliBefore.id, sliAfter.id)
        assertTrue { sliBefore.selected != sliAfter.selected }

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
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
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
        shoppingListViewModel.hydrate(getAisleGrouping(aisle.locationId), FilterType.ALL)
        val shoppingListItem = getAisleListItems(shoppingListViewModel).first { it.id == aisle.id }
        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        shoppingListViewModel.updateSelectedProductAisle(aisle.id)

        assertFalse(changeAisleCalled)
    }

    @Test
    fun requestLocationAisles_ValidLocation_EmitsAisleList() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)

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
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        shoppingListViewModel.requestLocationAisles()

        // Validate that aisles actually exist
        val aislesForLocation = shoppingListViewModel.aislesForLocation.first()
        assertTrue(aislesForLocation.isNotEmpty())

        shoppingListViewModel.clearLocationAisles()

        val aislesForLocationAfterClear = shoppingListViewModel.aislesForLocation.first()
        assertTrue(aislesForLocationAfterClear.isEmpty())
    }


    private suspend fun awaitUiStateError(
        viewModel: ShoppingListViewModel
    ): ShoppingListViewModel.ShoppingListUiState.Error {
        // We use first() to wait until the Flow emits a state that is Updated
        return viewModel.shoppingListUiState
            .first { it is ShoppingListViewModel.ShoppingListUiState.Error }
                as ShoppingListViewModel.ShoppingListUiState.Error
    }

    @Test
    fun updateSelectedProductAisle_AisleIsFromDifferentLocation_UiStateIsAisleronError() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val shoppingListItem = getProductListItems(shoppingListViewModel).first()
        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        val changeToAisle =
            get<AisleRepository>().getAll().first { it.locationId != shoppingList.id }

        val event = awaitEvent(shoppingListViewModel) {
            shoppingListViewModel.updateSelectedProductAisle(changeToAisle.id)
        }

        assert(event is ShoppingListViewModel.ShoppingListEvent.ShowError)

        val error = event as ShoppingListViewModel.ShoppingListEvent.ShowError
        Assert.assertEquals(AisleronException.ExceptionCode.AISLE_MOVE_EXCEPTION, error.errorCode)
    }

    @Test
    fun getSelectedItemAisleId_SingleSelectedItem_AisleIdReturned() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val shoppingListItem = getProductListItems(shoppingListViewModel).first()

        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        val aisleId = shoppingListViewModel.getSelectedItemAisleId()

        assertEquals(shoppingListItem.aisleId, aisleId)
    }

    @Test
    fun getSelectedItemAisleId_NoSelectedItems_ReturnMinus1() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)

        val aisleId = shoppingListViewModel.getSelectedItemAisleId()

        assertEquals(-1, aisleId)
    }

    @Test
    fun getSelectedItemAisleId_MultipleSelectedItems_ReturnMinus1() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val item1 = getProductListItems(shoppingListViewModel).first()
            .also { shoppingListViewModel.toggleItemSelection(it) }

        getProductListItems(shoppingListViewModel).first { it.id != item1.id }
            .also { shoppingListViewModel.toggleItemSelection(it) }

        val aisleId = shoppingListViewModel.getSelectedItemAisleId()

        assertEquals(-1, aisleId)
    }

    private suspend fun getAisleListItems(viewModel: ShoppingListViewModel): List<AisleShoppingListItem> =
        awaitUiStateUpdated(viewModel).shoppingList.filterIsInstance<AisleShoppingListItem>()


    private suspend fun getProductListItems(viewModel: ShoppingListViewModel): List<ProductShoppingListItem> =
        awaitUiStateUpdated(viewModel).shoppingList.filterIsInstance<ProductShoppingListItem>()

    @Test
    fun removeSelectedItems_MultipleItemsSelected_RemoveAllItems() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)

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
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)

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

    @Test
    fun shoppingListUiState_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on Get Shopping List"

        declare<GetShoppingListUseCase> {
            object : GetShoppingListUseCase {
                override fun invoke(locationId: Int, filter: ShoppingListFilter): Flow<Location?> =
                    flow {
                        throw Exception(exceptionMessage)
                    }

                override fun invoke(
                    locationType: LocationType, filter: ShoppingListFilter
                ): Flow<List<Location>> =
                    flow {
                        throw Exception(exceptionMessage)
                    }
            }
        }

        val vm = get<ShoppingListViewModel>()
        vm.hydrate(getAisleGrouping(1), FilterType.NEEDED)
        val uiState = awaitUiStateError(vm)
        assertEquals(exceptionMessage, uiState.errorMessage)
        assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, uiState.errorCode)
    }

    @Test
    fun hasSelectedItems_ItemsSelected_ReturnsTrue() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val shoppingListItem = getProductListItems(shoppingListViewModel).first()

        shoppingListViewModel.toggleItemSelection(shoppingListItem)

        assertTrue(shoppingListViewModel.hasSelectedItems())
    }

    @Test
    fun hasSelectedItems_NoItemsSelected_ReturnsFalse() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)

        assertFalse(shoppingListViewModel.hasSelectedItems())
    }

    @Test
    fun navigateToLoyaltyCard_LocationHasLoyaltyCard_EmitNavigateToLoyaltyCardEvent() = runTest {
        val shoppingList = getShoppingList()
        val loyaltyCardRepository = get<LoyaltyCardRepository>()
        val loyaltyCardId = loyaltyCardRepository.add(
            LoyaltyCard(
                id = 0,
                name = "Test Loyalty Card",
                provider = LoyaltyCardProviderType.CATIMA,
                intent = "Dummy Intent"
            )
        )

        loyaltyCardRepository.addToLocation(shoppingList.id, loyaltyCardId)
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val loyaltyCard = awaitLoyaltyCard(shoppingListViewModel)

        val event = awaitEvent(shoppingListViewModel) {
            shoppingListViewModel.navigateToLoyaltyCard()
        }

        assert(event is ShoppingListViewModel.ShoppingListEvent.NavigateToLoyaltyCard)

        val navEvent = event as ShoppingListViewModel.ShoppingListEvent.NavigateToLoyaltyCard
        assertEquals(loyaltyCard, navEvent.loyaltyCard)
    }

    @Test
    fun navigateToEditShop_LocationIdIsPopulated_EmitNavigateToEditShopEvent() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)

        val event = awaitEvent(shoppingListViewModel) {
            shoppingListViewModel.navigateToEditShop()
        }

        assert(event is ShoppingListViewModel.ShoppingListEvent.NavigateToEditLocation)

        val navEvent = event as ShoppingListViewModel.ShoppingListEvent.NavigateToEditLocation
        assertEquals(shoppingList.id, navEvent.locationId)
    }

    @Test
    fun navigateToEditShop_LocationIdNull_NoEventEmitted() = runTest {
        // Create a list to catch any events
        val collectedEvents = mutableListOf<ShoppingListViewModel.ShoppingListEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            shoppingListViewModel.events.toList(collectedEvents)
        }
        try {
            shoppingListViewModel.navigateToEditShop()

            // Force the scheduler to run any pending coroutines
            runCurrent()

            assertTrue(collectedEvents.isEmpty())
        } finally {
            job.cancel()
        }
    }


    @Test
    fun navigateToEditItem_NoItemsSelected_NoEventEmitted() = runTest {
        val collectedEvents = mutableListOf<ShoppingListViewModel.ShoppingListEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            shoppingListViewModel.events.toList(collectedEvents)
        }
        try {
            shoppingListViewModel.navigateToEditItem()

            // Force the scheduler to run any pending coroutines
            runCurrent()

            assertTrue(collectedEvents.isEmpty())
        } finally {
            job.cancel()
        }
    }

    @Test
    fun navigateToEditItem_MultipleItemsSelected_NoEventEmitted() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val listItems = getProductListItems(shoppingListViewModel)

        shoppingListViewModel.toggleItemSelection(listItems.first())
        shoppingListViewModel.toggleItemSelection(listItems.last())

        val collectedEvents = mutableListOf<ShoppingListViewModel.ShoppingListEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            shoppingListViewModel.events.toList(collectedEvents)
        }
        try {
            shoppingListViewModel.navigateToEditItem()

            // Force the scheduler to run any pending coroutines
            runCurrent()

            assertTrue(collectedEvents.isEmpty())
        } finally {
            job.cancel()
        }
    }

    @Test
    fun navigateToEditItem_ItemIsAisle_EmitNavigateToEditAisleEvent() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val item = getAisleListItems(shoppingListViewModel).first { !it.isDefault }

        shoppingListViewModel.toggleItemSelection(item)

        val event = awaitEvent(shoppingListViewModel) {
            shoppingListViewModel.navigateToEditItem()
        }

        assertEquals(
            ShoppingListViewModel.ShoppingListEvent.NavigateToEditAisle(item.id, shoppingList.id),
            event
        )
    }

    @Test
    fun navigateToEditItem_ItemIsProduct_EmitNavigateToEditProductEvent() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)
        val item = getProductListItems(shoppingListViewModel).first()

        shoppingListViewModel.toggleItemSelection(item)

        val event = awaitEvent(shoppingListViewModel) {
            shoppingListViewModel.navigateToEditItem()
        }

        assertEquals(
            ShoppingListViewModel.ShoppingListEvent.NavigateToEditProduct(item.id),
            event
        )
    }

    @Test
    fun navigateToEditItem_ItemIsLocation_EmitNavigateToEditLocationEvent() = runTest {
        //TODO: Implement when Location becomes selectable
    }

    @Test
    fun navigateToAddSingleAisle_HasAisleListCoordinator_EmitNavigateToAddSingleAisle() = runTest {
        val shoppingList = getShoppingList()
        shoppingListViewModel.hydrate(getAisleGrouping(shoppingList.id), shoppingList.defaultFilter)

        val event = awaitEvent(shoppingListViewModel) {
            shoppingListViewModel.navigateToAddSingleAisle()
        }

        assertEquals(
            ShoppingListViewModel.ShoppingListEvent.NavigateToAddSingleAisle(shoppingList.id),
            event
        )
    }

    @Test
    fun navigateToAddMultipleAisles_HasAisleListCoordinator_EmitNavigateToAddMultipleAislesEvent() =
        runTest {
            val shoppingList = getShoppingList()
            shoppingListViewModel.hydrate(
                getAisleGrouping(shoppingList.id),
                shoppingList.defaultFilter
            )

            val event = awaitEvent(shoppingListViewModel) {
                shoppingListViewModel.navigateToAddMultipleAisles()
            }

            assertEquals(
                ShoppingListViewModel.ShoppingListEvent.NavigateToAddMultipleAisles(shoppingList.id),
                event
            )
        }

    /**
     * TODO: Add tests for grouping by location
     */
}