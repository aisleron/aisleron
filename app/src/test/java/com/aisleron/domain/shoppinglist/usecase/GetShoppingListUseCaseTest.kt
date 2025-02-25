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

package com.aisleron.domain.shoppinglist.usecase

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.data.TestDataManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetShoppingListUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var getShoppingListUseCase: GetShoppingListUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getShoppingListUseCase =
            GetShoppingListUseCase(testData.getRepository<LocationRepository>())
    }

    @Test
    fun getShoppingList_NonExistentId_ReturnNull() {
        val shoppingList: Location? = runBlocking { getShoppingListUseCase(2001).first() }
        assertNull(shoppingList)
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocation() {
        val shoppingList: Location?
        runBlocking {
            val locationId = testData.getRepository<LocationRepository>().getAll().first().id
            shoppingList = getShoppingListUseCase(locationId).first()
        }
        assertNotNull(shoppingList)
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocationWithAisles() {
        val shoppingList: Location?
        runBlocking {
            val locationId = testData.getRepository<LocationRepository>().getAll().first().id
            shoppingList = getShoppingListUseCase(locationId).first()
        }
        assertTrue(shoppingList!!.aisles.isNotEmpty())
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocationWithProducts() {
        val shoppingList: Location?
        runBlocking {
            val locationId = testData.getRepository<LocationRepository>().getAll()
                .first { it.type == LocationType.SHOP }.id
            shoppingList = getShoppingListUseCase(locationId).first()
        }
        assertTrue(shoppingList!!.aisles.count { it.products.isNotEmpty() } > 0)
    }
}