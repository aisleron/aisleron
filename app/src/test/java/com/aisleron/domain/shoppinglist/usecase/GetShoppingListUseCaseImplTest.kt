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

package com.aisleron.domain.shoppinglist.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.shoppinglist.ShoppingListFilter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GetShoppingListUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getShoppingListUseCase: GetShoppingListUseCase
    private lateinit var locationRepository: LocationRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getShoppingListUseCase = dm.getUseCase()
        locationRepository = dm.getRepository()
    }

    @Test
    fun getShoppingList_NonExistentId_ReturnNull() = runTest {
        val shoppingList: Location? = getShoppingListUseCase(2001, ShoppingListFilter()).first()
        assertNull(shoppingList)
    }

    private suspend fun getShop(): Location {
        val locationId = locationRepository.getAll().first { it.type == LocationType.SHOP }.id
        return locationRepository.getLocationWithAislesWithProducts(locationId).first()!!
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocation() = runTest {
        val locationId = getShop().id

        val shoppingList = getShoppingListUseCase(locationId, ShoppingListFilter()).first()

        assertNotNull(shoppingList)
        assertTrue(shoppingList!!.aisles.isNotEmpty())
        assertTrue(shoppingList.aisles.count { it.products.isNotEmpty() } > 0)
    }

    @ParameterizedTest(name = "Test returned products when Product Filter is {0}")
    @MethodSource("showFilteredProducts")
    fun getShoppingList_LocationFilterTypeSet_OnlyShowStatusProducts(
        filterType: FilterType, haveInStockProducts: Boolean, haveNeededProducts: Boolean
    ) = runTest {
        val location = getShop().copy(defaultFilter = filterType)
        locationRepository.update(location)

        val shoppingList = getShoppingListUseCase(location.id, ShoppingListFilter()).first()!!

        val allReturnedProducts = shoppingList.aisles.flatMap { it.products }
        assertTrue(allReturnedProducts.isNotEmpty())
        assertEquals(haveNeededProducts, allReturnedProducts.any { !it.product.inStock })
        assertEquals(haveInStockProducts, allReturnedProducts.any { it.product.inStock })
    }

    @ParameterizedTest(name = "Test default aisle returned if showDefaultAisle is {0}")
    @MethodSource("showDefaultAisle")
    fun getShoppingList_LocationDefaultAisleSet_ShowDefaultAisleAccordingly(
        showDefaultAisle: Boolean
    ) = runTest {
        val location = getShop().copy(showDefaultAisle = showDefaultAisle)
        locationRepository.update(location)
        val shoppingListFilter = ShoppingListFilter(
            showEmptyAisles = true
        )

        val shoppingList = getShoppingListUseCase(location.id, shoppingListFilter).first()!!

        assertEquals(showDefaultAisle, shoppingList.aisles.any { it.isDefault })
    }

    @ParameterizedTest(name = "Test returned products when ShoppingListFilter Product Filter is {0}")
    @MethodSource("showFilteredProducts")
    fun getShoppingList_ShoppingListFilterFilterTypeSet_OnlyShowStatusProducts(
        filterType: FilterType, haveInStockProducts: Boolean, haveNeededProducts: Boolean
    ) = runTest {
        val location = getShop()
        val shoppingListFilter = ShoppingListFilter(
            productFilter = filterType,
            showEmptyAisles = true
        )

        val shoppingList = getShoppingListUseCase(location.id, shoppingListFilter).first()!!

        val allReturnedProducts = shoppingList.aisles.flatMap { it.products }
        assertTrue(allReturnedProducts.isNotEmpty())
        assertEquals(haveNeededProducts, allReturnedProducts.any { !it.product.inStock })
        assertEquals(haveInStockProducts, allReturnedProducts.any { it.product.inStock })
    }

    @Test
    fun getShoppingList_ShoppingListFilterShowEmptyAislesSet_ShowEmptyAisles() = runTest {
        val location = getShop()
        val productRepository = dm.getRepository<ProductRepository>()
        location.aisles.flatMap { it.products }.forEach {
            // Make sure all products are in stock so location aisles will be empty
            productRepository.update(it.product.copy(inStock = true))
        }

        val shoppingListFilter = ShoppingListFilter(
            showEmptyAisles = true
        )

        val shoppingList = getShoppingListUseCase(location.id, shoppingListFilter).first()!!

        val aisleCount = dm.getRepository<AisleRepository>().getForLocation(location.id).count()
        assertEquals(aisleCount, shoppingList.aisles.count())
    }

    @Test
    fun getShoppingList_ShoppingListFilterProductNameQuerySet_MatchingProductsReturned() = runTest {
        val location = getShop().copy(showDefaultAisle = false)
        locationRepository.update(location)

        val productRepository = dm.getRepository<ProductRepository>()
        location.aisles.flatMap { it.products }.forEach {
            // Make sure all products are available to query
            productRepository.update(it.product.copy(inStock = false))
        }

        val query = location.aisles.first().products.first().product.name
        val shoppingListFilter = ShoppingListFilter(
            productNameQuery = query,
            showEmptyAisles = true
        )

        val shoppingList = getShoppingListUseCase(location.id, shoppingListFilter).first()!!

        val allReturnedProducts = shoppingList.aisles.flatMap { it.products }
        assertTrue(allReturnedProducts.isNotEmpty())
        assertTrue(allReturnedProducts.all { it.product.name.contains(query, ignoreCase = true) })

        assertTrue(shoppingList.aisles.any { it.isDefault })
    }

    @Test
    fun getShoppingList_ProductHasNote_noteIncluded() = runTest {
        val location = getShop()

        val noteText = "This is a test note."
        val noteBefore = Note(
            id = dm.getRepository<NoteRepository>().add(Note(0, noteText)),
            noteText = noteText
        )

        val productBefore = location.aisles.flatMap { it.products }
            .first { !it.product.inStock }.product.copy(noteId = noteBefore.id)

        dm.getRepository<ProductRepository>().update(productBefore)

        val shoppingList = getShoppingListUseCase(location.id, ShoppingListFilter()).first()!!

        val productAfter = shoppingList.aisles.flatMap { it.products }
            .first { it.product.id == productBefore.id }.product

        assertEquals(noteBefore, productAfter.note)
    }

    private companion object {
        @JvmStatic
        fun showFilteredProducts(): Stream<Arguments> = Stream.of(
            // FilterType, haveInStockProducts, haveNeededProducts
            Arguments.of(FilterType.NEEDED, false, true),
            Arguments.of(FilterType.IN_STOCK, true, false),
            Arguments.of(FilterType.ALL, true, true)
        )

        @JvmStatic
        fun showDefaultAisle(): Stream<Arguments> = Stream.of(
            // showDefaultAisle
            Arguments.of(true),
            Arguments.of(false)
        )
    }

    /**
     * Tests for all filtering options:
     * * productQuery filters products
     */
}