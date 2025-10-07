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

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetShoppingListUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getShoppingListUseCase: GetShoppingListUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getShoppingListUseCase = dm.getUseCase()
    }

    @Test
    fun getShoppingList_NonExistentId_ReturnNull() = runTest {
        val shoppingList: Location? = runBlocking { getShoppingListUseCase(2001).first() }
        assertNull(shoppingList)
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocation() = runTest {
        val locationId = dm.getRepository<LocationRepository>().getAll().first().id

        val shoppingList = getShoppingListUseCase(locationId).first()

        assertNotNull(shoppingList)
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocationWithAisles() = runTest {
        val locationId = dm.getRepository<LocationRepository>().getAll().first().id

        val shoppingList = getShoppingListUseCase(locationId).first()

        assertTrue(shoppingList!!.aisles.isNotEmpty())
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocationWithProducts() = runTest {
        val locationId = dm.getRepository<LocationRepository>().getAll()
            .first { it.type == LocationType.SHOP }.id

        val shoppingList = getShoppingListUseCase(locationId).first()

        assertTrue(shoppingList!!.aisles.count { it.products.isNotEmpty() } > 0)
    }
}