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

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoveAisleUseCaseTest {

    private lateinit var dm: TestDependencyManager
    private lateinit var removeAisleUseCase: RemoveAisleUseCase
    private lateinit var existingAisle: Aisle

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        val aisleRepository = dm.getRepository<AisleRepository>()
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        removeAisleUseCase = dm.getUseCase()

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
    fun removeAisle_IsDefaultAisle_ThrowsException() = runTest {
        val defaultAisle = dm.getRepository<AisleRepository>().getDefaultAisles().first()

        assertThrows<AisleronException.DeleteDefaultAisleException> {
            removeAisleUseCase(defaultAisle)
        }
    }

    @Test
    fun removeAisle_IsExistingNonDefaultAisle_AisleRemoved() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val countBefore = aisleRepository.getAll().count()

        removeAisleUseCase(existingAisle)

        val removedAisle = aisleRepository.get(existingAisle.id)
        assertNull(removedAisle)

        val countAfter = aisleRepository.getAll().count()
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeAisle_HasNoDefaultAisle_RemoveAisleProducts() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val aisleProductCountBefore = aisleProductRepository.getAll().count()
        val defaultAisle = aisleRepository.getDefaultAisleFor(existingAisle.locationId)
        defaultAisle?.let { aisleRepository.remove(it) }
        val aisleProductCount =
            aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }

        removeAisleUseCase(existingAisle)

        val aisleProductCountAfter = aisleProductRepository.getAll().count()
        assertEquals(aisleProductCountBefore - aisleProductCount, aisleProductCountAfter)
    }

    @Test
    fun removeAisle_HasDefaultAisle_MoveAisleProducts() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val aisleProductCountBefore = aisleProductRepository.getAll().count()
        val defaultAisle =
            aisleRepository.getDefaultAisleFor(existingAisle.locationId)!!

        val aisleProductCount =
            aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }

        val defaultAisleProductCountBefore =
            aisleProductRepository.getAll().count { it.aisleId == defaultAisle.id }

        removeAisleUseCase(existingAisle)

        val aisleProductCountAfter = aisleProductRepository.getAll().count()
        assertEquals(aisleProductCountBefore, aisleProductCountAfter)

        val defaultAisleProductCountAfter =
            aisleProductRepository.getAll().count { it.aisleId == defaultAisle.id }

        assertEquals(
            defaultAisleProductCountBefore + aisleProductCount,
            defaultAisleProductCountAfter
        )
    }

    @Test
    fun removeAisle_AisleHasNoProducts_AisleRemoved() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val emptyAisleId = aisleRepository.add(
            Aisle(
                name = "Empty Aisle",
                products = emptyList(),
                locationId = dm.getRepository<LocationRepository>().getAll().first().id,
                rank = 1000,
                id = 0,
                isDefault = false,
                expanded = true
            )
        )
        val emptyAisle = aisleRepository.get(emptyAisleId)!!
        val countBefore = aisleRepository.getAll().count()

        removeAisleUseCase(emptyAisle)

        val removedAisle = aisleRepository.get(emptyAisle.id)
        assertNull(removedAisle)

        val countAfter = aisleRepository.getAll().count()
        assertEquals(countBefore - 1, countAfter)
    }
}