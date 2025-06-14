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

package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoveAisleUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var removeAisleUseCase: RemoveAisleUseCase
    private lateinit var existingAisle: Aisle

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val aisleRepository = testData.getRepository<AisleRepository>()
        val aisleProductRepository = testData.getRepository<AisleProductRepository>()
        removeAisleUseCase = RemoveAisleUseCaseImpl(
            aisleRepository,
            UpdateAisleProductsUseCase(aisleProductRepository),
            RemoveProductsFromAisleUseCase(aisleProductRepository),
        )

        runBlocking {
            existingAisle = aisleRepository.getAll().first { !it.isDefault }
            val defaultAisle =
                aisleRepository.getDefaultAisleFor(existingAisle.locationId)!!
            val aisleProducts =
                aisleProductRepository.getAll().filter { it.aisleId == defaultAisle.id }

            aisleProducts.forEach {
                val moveAisle = it.copy(aisleId = existingAisle.id)
                aisleProductRepository.update(moveAisle)
            }
        }
    }

    @Test
    fun removeAisle_IsDefaultAisle_ThrowsException() {
        runBlocking {
            val defaultAisle = testData.getRepository<AisleRepository>().getDefaultAisles().first()

            assertThrows<AisleronException.DeleteDefaultAisleException> {
                removeAisleUseCase(defaultAisle)
            }
        }
    }

    @Test
    fun removeAisle_IsExistingNonDefaultAisle_AisleRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedAisle: Aisle?
        runBlocking {
            val aisleRepository = testData.getRepository<AisleRepository>()
            countBefore = aisleRepository.getAll().count()
            removeAisleUseCase(existingAisle)
            removedAisle = aisleRepository.get(existingAisle.id)
            countAfter = aisleRepository.getAll().count()
        }
        assertNull(removedAisle)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeAisle_HasNoDefaultAisle_RemoveAisleProducts() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCount: Int
        runBlocking {
            val aisleRepository = testData.getRepository<AisleRepository>()
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            val defaultAisle = aisleRepository.getDefaultAisleFor(existingAisle.locationId)
            defaultAisle?.let { aisleRepository.remove(it) }
            aisleProductCount =
                aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }

            aisleProductCountBefore = aisleProductRepository.getAll().count()

            removeAisleUseCase(existingAisle)

            aisleProductCountAfter = aisleProductRepository.getAll().count()

        }
        assertEquals(aisleProductCountBefore - aisleProductCount, aisleProductCountAfter)
    }

    @Test
    fun removeAisle_HasDefaultAisle_MoveAisleProducts() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCount: Int
        val defaultAisleProductCountBefore: Int
        val defaultAisleProductCountAfter: Int
        runBlocking {
            val aisleRepository = testData.getRepository<AisleRepository>()
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            val defaultAisle =
                aisleRepository.getDefaultAisleFor(existingAisle.locationId)!!
            aisleProductCount =
                aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }
            defaultAisleProductCountBefore =
                aisleProductRepository.getAll().count { it.aisleId == defaultAisle.id }

            aisleProductCountBefore = aisleProductRepository.getAll().count()

            removeAisleUseCase(existingAisle)

            aisleProductCountAfter = aisleProductRepository.getAll().count()

            defaultAisleProductCountAfter =
                aisleProductRepository.getAll().count { it.aisleId == defaultAisle.id }


        }
        assertEquals(aisleProductCountBefore, aisleProductCountAfter)
        assertEquals(
            defaultAisleProductCountBefore + aisleProductCount,
            defaultAisleProductCountAfter
        )
    }

    @Test
    fun removeAisle_AisleHasNoProducts_AisleRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedAisle: Aisle?
        runBlocking {
            val aisleRepository = testData.getRepository<AisleRepository>()
            val emptyAisleId = aisleRepository.add(
                Aisle(
                    name = "Empty Aisle",
                    products = emptyList(),
                    locationId = testData.getRepository<LocationRepository>().getAll().first().id,
                    rank = 1000,
                    id = 0,
                    isDefault = false,
                    expanded = true
                )
            )
            val emptyAisle = aisleRepository.get(emptyAisleId)!!
            countBefore = aisleRepository.getAll().count()
            removeAisleUseCase(emptyAisle)
            removedAisle = aisleRepository.get(emptyAisle.id)
            countAfter = aisleRepository.getAll().count()
        }
        assertNull(removedAisle)
        assertEquals(countBefore - 1, countAfter)
    }
}