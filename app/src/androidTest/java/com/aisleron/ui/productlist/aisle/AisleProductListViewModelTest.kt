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

package com.aisleron.ui.productlist.aisle

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
import com.aisleron.domain.productlist.ProductListFilter
import com.aisleron.domain.productlist.usecase.GetAisleProductListUseCase
import com.aisleron.ui.productlist.ProductShoppingListItem
import com.aisleron.ui.productlist.ShoppingListItem
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
class AisleProductListViewModelTest : KoinTest {
    private lateinit var aisleProductListViewModel: AisleProductListViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        aisleProductListViewModel = get<AisleProductListViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun hydrate_IsValidLocation_LocationMembersAreCorrect() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()

        aisleProductListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)

        val result = awaitUiStateUpdated(aisleProductListViewModel)
        assertEquals(existingLocation.name, result.locationName)
        assertEquals(existingLocation.defaultFilter, result.productFilter)
        assertEquals(existingLocation.type, result.locationType)

        assertEquals(existingLocation.defaultFilter, aisleProductListViewModel.productFilter)
        assertEquals(existingLocation.id, aisleProductListViewModel.locationId.value)
    }

    @Test
    fun hydrate_IsInvalidLocation_LocationMembersAreDefault() = runTest {
        aisleProductListViewModel.hydrate(-1, FilterType.NEEDED)

        val result = awaitUiStateUpdated(aisleProductListViewModel)

        Assert.assertEquals("", result.locationName)
        Assert.assertEquals(FilterType.NEEDED, result.productFilter)
        Assert.assertEquals(LocationType.HOME, result.locationType)
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
        aisleProductListViewModel.hydrate(locationId, location.defaultFilter)

        val productList = awaitUiStateUpdated(aisleProductListViewModel).productList

        assertEquals(1, productList.count())
        assertEquals(1, productList.count { it.itemType == ShoppingListItem.ItemType.EMPTY_LIST })
    }

    @Test
    fun hydrate_ListHasAislesAndProducts_EmptyListItemExcluded() = runTest {
        val location = getProductList()
        aisleProductListViewModel.hydrate(location.id, location.defaultFilter)

        val productList = awaitUiStateUpdated(aisleProductListViewModel).productList

        assertTrue(productList.isNotEmpty())
        assertEquals(0, productList.count { it.itemType == ShoppingListItem.ItemType.EMPTY_LIST })
    }

    private suspend fun awaitUiStateUpdated(
        viewModel: AisleProductListViewModel
    ): AisleProductListViewModel.UiState.Updated {
        // We use first() to wait until the Flow emits a state that is Updated
        return viewModel.uiState
            .first { it is AisleProductListViewModel.UiState.Updated }
                as AisleProductListViewModel.UiState.Updated
    }

    private suspend fun awaitLoyaltyCard(viewModel: AisleProductListViewModel): LoyaltyCard? {
        return viewModel.loyaltyCard.first()
    }

    @Test
    fun removeItem_SelectedItemsIsDefaultAisle_UiStateIsError() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        aisleProductListViewModel.setShowEmptyAisles(true)
        val existingAisle = getAisleListItems(aisleProductListViewModel).first { it.isDefault }
        aisleProductListViewModel.toggleItemSelection(existingAisle)

        val event = awaitEvent(aisleProductListViewModel) {
            aisleProductListViewModel.removeSelectedItems()
        }

        assert(event is AisleProductListViewModel.UiEvent.ShowError)
    }

    private suspend fun updateProductStatusArrangeAct(newInStock: Boolean): Product? {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, FilterType.ALL)
        val shoppingListItem =
            getProductListItems(aisleProductListViewModel).first { it.inStock == newInStock }

        aisleProductListViewModel.updateProductStatus(shoppingListItem, newInStock)
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
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        val shoppingListItem = getAisleListItems(aisleProductListViewModel).first()

        aisleProductListViewModel.updateAisleExpanded(shoppingListItem, expanded)

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
        val domainShoppingList = getProductList()
        aisleProductListViewModel.hydrate(domainShoppingList.id, domainShoppingList.defaultFilter)
        val productListBefore = awaitUiStateUpdated(aisleProductListViewModel).productList

        val aisleSummaryBefore =
            productListBefore.groupingBy { it.aisleId }.eachCount().maxBy { it.value }

        val shoppingListItem = productListBefore.filterIsInstance<AisleShoppingListItem>().first {
            it.aisleId == aisleSummaryBefore.key
        }

        aisleProductListViewModel.updateAisleExpanded(shoppingListItem, false)

        assertTrue(aisleSummaryBefore.value > 1)

        // Create a new instance of the viewmodel to verify results because hydrate won't run again.
        val vm = get<AisleProductListViewModel>()
        vm.hydrate(domainShoppingList.id, domainShoppingList.defaultFilter)
        val productListAfter = awaitUiStateUpdated(vm).productList
        val aisleCountAfter = productListAfter.count { it.aisleId == aisleSummaryBefore.key }
        assertEquals(1, aisleCountAfter)
    }

    private suspend fun getProductList(): Location {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        return locationRepo.getLocationWithAislesWithProducts(locationId).first()!!
    }

    @Test
    fun submitProductSearch_ProductsMatch_UiStateHasProducts() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val searchString = "Apple"
        val productSearchCount =
            get<ProductRepository>().getAll().count { it.name.contains(searchString) }

        aisleProductListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        aisleProductListViewModel.submitProductSearch(searchString)

        val productList = awaitUiStateUpdated(aisleProductListViewModel).productList
        Assert.assertEquals(
            productSearchCount,
            productList.count { p -> p.name.contains(searchString) && p.itemType == ShoppingListItem.ItemType.PRODUCT }
        )
    }

    @Test
    fun submitProductSearch_NoProductsMatch_UiStateHasNoProducts() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val searchString = "No Product Name Matches This String Woo Yeah"
        val productSearchCount = 0

        aisleProductListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        aisleProductListViewModel.submitProductSearch(searchString)

        val productList = awaitUiStateUpdated(aisleProductListViewModel).productList
        Assert.assertEquals(
            productSearchCount,
            productList.count { p -> p.name.contains(searchString) && p.itemType == ShoppingListItem.ItemType.PRODUCT }
        )
    }

    @Test
    fun requestListRefresh_returnDefaultListIsFalse_DoNotUpdateFilters() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id

        aisleProductListViewModel.hydrate(locationId, FilterType.ALL, false)

        val aisleCount = getAisleListItems(aisleProductListViewModel).count()
        val productCount = getProductListItems(aisleProductListViewModel).count()

        aisleProductListViewModel.submitProductSearch("Ap")

        val searchedAisleCount = getAisleListItems(aisleProductListViewModel).count()
        assertTrue(aisleCount > searchedAisleCount)

        val searchedProductCount = getProductListItems(aisleProductListViewModel).count()
        assertTrue(productCount > searchedProductCount)

        aisleProductListViewModel.requestListRefresh(false)

        val refreshedAisleCount = getAisleListItems(aisleProductListViewModel).count()
        assertEquals(searchedAisleCount, refreshedAisleCount)

        val refreshedProductCount = getProductListItems(aisleProductListViewModel).count()
        assertEquals(searchedProductCount, refreshedProductCount)
    }

    @Test
    fun requestListRefresh_returnDefaultListIsTrue_ReturnDefaultList() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id

        aisleProductListViewModel.hydrate(locationId, FilterType.ALL, false)

        val aisleCount = getAisleListItems(aisleProductListViewModel).count()
        val productCount = getProductListItems(aisleProductListViewModel).count()

        aisleProductListViewModel.submitProductSearch("Ap")

        val searchedAisleCount = getAisleListItems(aisleProductListViewModel).count()
        assertTrue(aisleCount > searchedAisleCount)

        val searchedProductCount = getProductListItems(aisleProductListViewModel).count()
        assertTrue(productCount > searchedProductCount)

        aisleProductListViewModel.requestListRefresh(true)

        val refreshedAisleCount = getAisleListItems(aisleProductListViewModel).count()
        assertEquals(aisleCount, refreshedAisleCount)

        val refreshedProductCount = getProductListItems(aisleProductListViewModel).count()
        assertEquals(productCount, refreshedProductCount)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_AisleProductListViewModelReturned() {
        val vm = AisleProductListViewModel(
            get<GetAisleProductListUseCase>(),
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

    private suspend fun TestScope.awaitEvent(
        viewModel: AisleProductListViewModel,
        trigger: suspend () -> Unit
    ): AisleProductListViewModel.UiEvent {
        // We create a "trap" for the event before we trigger the action
        val events = viewModel.events

        // Start collecting in the background
        val deferred = CompletableDeferred<AisleProductListViewModel.UiEvent>()
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

        val vm = get<AisleProductListViewModel>()
        vm.hydrate(1, FilterType.NEEDED)
        val sli = getAisleListItems(vm).first()
        vm.toggleItemSelection(sli)

        val event = awaitEvent(vm) {
            vm.removeSelectedItems()
        }

        assert(event is AisleProductListViewModel.UiEvent.ShowError)

        val error = event as AisleProductListViewModel.UiEvent.ShowError
        assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, error.errorCode)
        assertEquals(exceptionMessage, error.errorMessage)
    }

    @Test
    fun updateItemRank_ItemIsAisle_AisleRankUpdated() = runTest {
        val productList = getProductList()
        val movedAisle = productList.aisles.last { !it.isDefault }
        val precedingAisle = productList.aisles.first { !it.isDefault && it.id != movedAisle.id }
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        aisleProductListViewModel.setShowEmptyAisles(true)

        val shoppingListItem =
            getAisleListItems(aisleProductListViewModel).first { it.id == movedAisle.id }

        val precedingItem =
            getAisleListItems(aisleProductListViewModel).first { it.id == precedingAisle.id }

        aisleProductListViewModel.updateItemRank(shoppingListItem, precedingItem)

        val updatedAisle = get<AisleRepository>().get(movedAisle.id)
        Assert.assertEquals(precedingItem.rank + 1, updatedAisle?.rank)
    }

    @Test
    fun removeItem_SelectedItemsIsStandardAisle_AisleRemoved() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        aisleProductListViewModel.setShowEmptyAisles(true)
        val shoppingListItem = getAisleListItems(aisleProductListViewModel).first { !it.isDefault }
        aisleProductListViewModel.toggleItemSelection(shoppingListItem)

        aisleProductListViewModel.removeSelectedItems()

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
        aisleProductListViewModel.hydrate(locationId, location.defaultFilter)

        return getAisleListItems(aisleProductListViewModel).firstOrNull { it.isDefault }
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

        aisleProductListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        aisleProductListViewModel.sortListByName()

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

        val vm = get<AisleProductListViewModel>()
        vm.hydrate(1, FilterType.NEEDED)

        val event = awaitEvent(vm) {
            vm.sortListByName()
        }

        assert(event is AisleProductListViewModel.UiEvent.ShowError)

        val error = event as AisleProductListViewModel.UiEvent.ShowError
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

        aisleProductListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)

        val loyaltyCardResult = awaitLoyaltyCard(aisleProductListViewModel)
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

        aisleProductListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)

        val loyaltyCardResult = awaitLoyaltyCard(aisleProductListViewModel)
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

        aisleProductListViewModel.hydrate(existingLocation.id, existingLocation.defaultFilter)
        val productList = awaitUiStateUpdated(aisleProductListViewModel).productList

        val item = productList.first { it.itemType == ShoppingListItem.ItemType.AISLE }

        aisleProductListViewModel.movedItem(item)

        val fullProductList = awaitUiStateUpdated(aisleProductListViewModel).productList

        assertTrue { productList.count() < fullProductList.count() }
        assertNull(productList.firstOrNull { it.itemType == ShoppingListItem.ItemType.AISLE && it.name == aisleName })
        assertNotNull(fullProductList.firstOrNull { it.itemType == ShoppingListItem.ItemType.AISLE && it.name == aisleName })
    }

    @Test
    fun setShowEmptyAisles_ValueChanged_UiStateIsUpdated() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val showEmptyAisles = false

        aisleProductListViewModel.hydrate(locationId, FilterType.ALL, showEmptyAisles)
        val aisleCount = getAisleListItems(aisleProductListViewModel).count()
        val uiStateBefore = aisleProductListViewModel.uiState.value

        aisleProductListViewModel.setShowEmptyAisles(!showEmptyAisles)

        val uiStateAfter = aisleProductListViewModel.uiState.value
        assertNotEquals(uiStateBefore, uiStateAfter)

        val aisleCountAfter = getAisleListItems(aisleProductListViewModel).count()
        assertTrue(aisleCount < aisleCountAfter)
    }

    @Test
    fun setShowEmptyAisles_ValueUnchanged_UiStateIsNotUpdated() = runTest {
        val locationRepo = get<LocationRepository>()
        val locationId = locationRepo.getAll().first { it.type == LocationType.SHOP }.id
        val showEmptyAisles = false

        aisleProductListViewModel.hydrate(locationId, FilterType.ALL, showEmptyAisles)
        val uiStateBefore = aisleProductListViewModel.uiState.value

        aisleProductListViewModel.setShowEmptyAisles(showEmptyAisles)

        val uiStateAfter = aisleProductListViewModel.uiState.value
        assertEquals(uiStateBefore, uiStateAfter)
    }

    private suspend fun updateProductNeededQuantityArrangeAct(
        qtyInitial: Double, qtyNew: Double?
    ): Int {
        val productList = getProductList()
        val existingAisle =
            productList.aisles.first { it.products.count { p -> !p.product.inStock } > 0 }

        val existingProduct = existingAisle.products.first { !it.product.inStock }.product
        get<ProductRepository>().update(existingProduct.copy(qtyNeeded = qtyInitial))
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        val shoppingListItem =
            getProductListItems(aisleProductListViewModel).first { it.id == existingProduct.id }

        aisleProductListViewModel.updateProductNeededQuantity(shoppingListItem, qtyNew)

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

        val event = awaitEvent(aisleProductListViewModel) {
            updateProductNeededQuantityArrangeAct(qtyInitial, qtyNew)
        }

        assert(event is AisleProductListViewModel.UiEvent.ShowError)
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
        aisleProductListViewModel.hydrate(locationId, FilterType.ALL)

        aisleProductListViewModel.expandCollapseAisles()
        val expandedAfter = aisleRepository.getAll().count { it.expanded }

        assertTrue(expandedBefore > expandedAfter)
    }

    @Test
    fun selectedItems_SetAndClear_HandledCorrectly() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        aisleProductListViewModel.hydrate(aisle.locationId, FilterType.ALL)
        val shoppingListItem = getAisleListItems(aisleProductListViewModel).first { it.id == aisle.id }

        aisleProductListViewModel.toggleItemSelection(shoppingListItem)

        assertEquals(
            shoppingListItem.copyWith(selected = true),
            aisleProductListViewModel.selectedListItems.first()
        )

        aisleProductListViewModel.clearSelectedListItems()
        assertNull(aisleProductListViewModel.selectedListItems.firstOrNull())
    }

    @Test
    fun updateSelectedProductAisle_ItemIsProduct_ProductAisleUpdated() = runTest {
        val productList = getProductList()
        val existingAisle =
            productList.aisles.first { it.products.count { p -> !p.product.inStock } > 0 }

        val aisleProduct = existingAisle.products.first { !it.product.inStock }
        val existingProduct = aisleProduct.product
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        val shoppingListItem =
            getProductListItems(aisleProductListViewModel).first { it.id == existingProduct.id }

        aisleProductListViewModel.toggleItemSelection(shoppingListItem)

        val newAisle = productList.aisles.first { it.id != existingAisle.id }
        aisleProductListViewModel.updateSelectedProductAisle(newAisle.id)

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
        aisleProductListViewModel.hydrate(aisle.locationId, FilterType.ALL)
        val shoppingListItem = getAisleListItems(aisleProductListViewModel).first { it.id == aisle.id }
        aisleProductListViewModel.toggleItemSelection(shoppingListItem)

        aisleProductListViewModel.updateSelectedProductAisle(aisle.id)

        assertFalse(changeAisleCalled)
    }

    @Test
    fun requestLocationAisles_ValidLocation_EmitsAisleList() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)

        aisleProductListViewModel.requestLocationAisles()

        val aislesForLocation = aisleProductListViewModel.aislesForLocation.first()
        assertTrue(aislesForLocation.isNotEmpty())

        // Verify aisles are sorted by rank
        val sortedAisles = productList.aisles.sortedBy { it.rank }
        assertEquals(sortedAisles.size, aislesForLocation.size)
        sortedAisles.forEachIndexed { index, aisle ->
            assertEquals(aisle.id, aislesForLocation[index].id)
            assertEquals(aisle.name, aislesForLocation[index].name)
        }
    }

    @Test
    fun clearLocationAisles_LocationAislesListCleared() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        aisleProductListViewModel.requestLocationAisles()

        // Validate that aisles actually exist
        val aislesForLocation = aisleProductListViewModel.aislesForLocation.first()
        assertTrue(aislesForLocation.isNotEmpty())

        aisleProductListViewModel.clearLocationAisles()

        val aislesForLocationAfterClear = aisleProductListViewModel.aislesForLocation.first()
        assertTrue(aislesForLocationAfterClear.isEmpty())
    }


    private suspend fun awaitUiStateError(
        viewModel: AisleProductListViewModel
    ): AisleProductListViewModel.UiState.Error {
        // We use first() to wait until the Flow emits a state that is Updated
        return viewModel.uiState
            .first { it is AisleProductListViewModel.UiState.Error }
                as AisleProductListViewModel.UiState.Error
    }

    @Test
    fun updateSelectedProductAisle_AisleIsFromDifferentLocation_UiStateIsAisleronError() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        val shoppingListItem = getProductListItems(aisleProductListViewModel).first()
        aisleProductListViewModel.toggleItemSelection(shoppingListItem)

        val changeToAisle =
            get<AisleRepository>().getAll().first { it.locationId != productList.id }

        val event = awaitEvent(aisleProductListViewModel) {
            aisleProductListViewModel.updateSelectedProductAisle(changeToAisle.id)
        }

        assert(event is AisleProductListViewModel.UiEvent.ShowError)

        val error = event as AisleProductListViewModel.UiEvent.ShowError
        Assert.assertEquals(AisleronException.ExceptionCode.AISLE_MOVE_EXCEPTION, error.errorCode)
    }

    @Test
    fun getSelectedItemAisleId_SingleSelectedItem_AisleIdReturned() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        val shoppingListItem = getProductListItems(aisleProductListViewModel).first()

        aisleProductListViewModel.toggleItemSelection(shoppingListItem)

        val aisleId = aisleProductListViewModel.getSelectedItemAisleId()

        assertEquals(shoppingListItem.aisleId, aisleId)
    }

    @Test
    fun getSelectedItemAisleId_NoSelectedItems_ReturnMinus1() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)

        val aisleId = aisleProductListViewModel.getSelectedItemAisleId()

        assertEquals(-1, aisleId)
    }

    @Test
    fun getSelectedItemAisleId_MultipleSelectedItems_ReturnMinus1() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        val item1 = getProductListItems(aisleProductListViewModel).first()
            .also { aisleProductListViewModel.toggleItemSelection(it) }

        getProductListItems(aisleProductListViewModel).first { it.id != item1.id }
            .also { aisleProductListViewModel.toggleItemSelection(it) }

        val aisleId = aisleProductListViewModel.getSelectedItemAisleId()

        assertEquals(-1, aisleId)
    }

    private suspend fun getAisleListItems(viewModel: AisleProductListViewModel): List<AisleShoppingListItem> =
        awaitUiStateUpdated(viewModel).productList.filterIsInstance<AisleShoppingListItem>()


    private suspend fun getProductListItems(viewModel: AisleProductListViewModel): List<ProductShoppingListItem> =
        awaitUiStateUpdated(viewModel).productList.filterIsInstance<ProductShoppingListItem>()

    @Test
    fun removeSelectedItems_MultipleItemsSelected_RemoveAllItems() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)

        val aisleItem = getAisleListItems(aisleProductListViewModel).first { !it.isDefault }
            .also { aisleProductListViewModel.toggleItemSelection(it) }

        val productItem = getProductListItems(aisleProductListViewModel).first()
            .also { aisleProductListViewModel.toggleItemSelection(it) }

        // Validate that the aisle and product exist in the repository
        assertNotNull(get<AisleRepository>().get(aisleItem.id))
        assertNotNull(get<ProductRepository>().get(productItem.id))

        aisleProductListViewModel.removeSelectedItems()

        val removedAisle = get<AisleRepository>().get(aisleItem.id)
        assertNull(removedAisle)

        val removedProduct = get<ProductRepository>().get(productItem.id)
        assertNull(removedProduct)
    }

    @Test
    fun updateSelectedProductAisle_MultipleItemsSelected_UpdateAllItems() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)

        val productOne = getProductListItems(aisleProductListViewModel).first()
            .also { aisleProductListViewModel.toggleItemSelection(it) }

        val productTwo = getProductListItems(aisleProductListViewModel).first { it.id != productOne.id }
            .also { aisleProductListViewModel.toggleItemSelection(it) }

        // Validate that the products exist in the repository
        val productRepository = get<ProductRepository>()
        assertNotNull(productRepository.get(productOne.id))
        assertNotNull(productRepository.get(productTwo.id))

        val newAisle = get<AisleRepository>().getAll()
            .first { it.locationId == productList.id && it.id != productOne.aisleId && it.id != productTwo.aisleId }

        aisleProductListViewModel.updateSelectedProductAisle(newAisle.id)

        val aisleProductRepository = get<AisleProductRepository>()
        val updatedProductOne = aisleProductRepository.getProductAisles(productOne.id)
            .singleOrNull { it.aisleId == newAisle.id }

        assertNotNull(updatedProductOne)

        val updatedProductTwo = aisleProductRepository.getProductAisles(productTwo.id)
            .singleOrNull { it.aisleId == newAisle.id }

        assertNotNull(updatedProductTwo)
    }

    @Test
    fun uiState_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on Get Shopping List"

        declare<GetAisleProductListUseCase> {
            object : GetAisleProductListUseCase {
                override fun invoke(locationId: Int, filter: ProductListFilter): Flow<Location?> =
                    flow {
                        throw Exception(exceptionMessage)
                    }
            }
        }

        val vm = get<AisleProductListViewModel>()
        vm.hydrate(1, FilterType.NEEDED)
        val uiState = awaitUiStateError(vm)
        assertEquals(exceptionMessage, uiState.errorMessage)
        assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, uiState.errorCode)
    }

    @Test
    fun hasSelectedItems_ItemsSelected_ReturnsTrue() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        val shoppingListItem = getProductListItems(aisleProductListViewModel).first()

        aisleProductListViewModel.toggleItemSelection(shoppingListItem)

        assertTrue(aisleProductListViewModel.hasSelectedItems())
    }

    @Test
    fun hasSelectedItems_NoItemsSelected_ReturnsFalse() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)

        assertFalse(aisleProductListViewModel.hasSelectedItems())
    }

    @Test
    fun navigateToLoyaltyCard_LocationHasLoyaltyCard_NavigateToLoyaltyCardEventEmitted() = runTest {
        val productList = getProductList()
        val loyaltyCardRepository = get<LoyaltyCardRepository>()
        val loyaltyCardId = loyaltyCardRepository.add(
            LoyaltyCard(
                id = 0,
                name = "Test Loyalty Card",
                provider = LoyaltyCardProviderType.CATIMA,
                intent = "Dummy Intent"
            )
        )

        loyaltyCardRepository.addToLocation(productList.id, loyaltyCardId)
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)
        val loyaltyCard = awaitLoyaltyCard(aisleProductListViewModel)

        val event = awaitEvent(aisleProductListViewModel) {
            aisleProductListViewModel.navigateToLoyaltyCard()
        }

        assert(event is AisleProductListViewModel.UiEvent.NavigateToLoyaltyCard)

        val navEvent = event as AisleProductListViewModel.UiEvent.NavigateToLoyaltyCard
        assertEquals(loyaltyCard, navEvent.loyaltyCard)
    }

    @Test
    fun navigateToLoyaltyCard_CardIsNull_NoEventEmitted() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)

        // Create a list to catch any events
        val collectedEvents = mutableListOf<AisleProductListViewModel.UiEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            aisleProductListViewModel.events.toList(collectedEvents)
        }
        try {
            aisleProductListViewModel.navigateToLoyaltyCard()

            // Force the scheduler to run any pending coroutines
            runCurrent()

            assertTrue(collectedEvents.isEmpty())
        } finally {
            job.cancel()
        }
    }

    @Test
    fun navigateToEditShop_LocationIdIsPopulated_NavigateToEditShopEventEmitted() = runTest {
        val productList = getProductList()
        aisleProductListViewModel.hydrate(productList.id, productList.defaultFilter)

        val event = awaitEvent(aisleProductListViewModel) {
            aisleProductListViewModel.navigateToEditShop()
        }

        assert(event is AisleProductListViewModel.UiEvent.NavigateToEditShop)

        val navEvent = event as AisleProductListViewModel.UiEvent.NavigateToEditShop
        assertEquals(productList.id, navEvent.id)
    }

    @Test
    fun navigateToEditShop_LocationIdNull_NoEventEmitted() = runTest {
        // Create a list to catch any events
        val collectedEvents = mutableListOf<AisleProductListViewModel.UiEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            aisleProductListViewModel.events.toList(collectedEvents)
        }
        try {
            aisleProductListViewModel.navigateToEditShop()

            // Force the scheduler to run any pending coroutines
            runCurrent()

            assertTrue(collectedEvents.isEmpty())
        } finally {
            job.cancel()
        }
    }
}