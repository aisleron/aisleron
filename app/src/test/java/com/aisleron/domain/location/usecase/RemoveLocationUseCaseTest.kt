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

package com.aisleron.domain.location.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveLocationUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var removeLocationUseCase: RemoveLocationUseCase
    private lateinit var existingLocation: Location

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        removeLocationUseCase = dm.getUseCase()
        runBlocking {
            existingLocation = dm.getRepository<LocationRepository>().get(1)!!
        }
    }

    @Test
    fun removeLocation_IsExistingLocation_LocationRemoved() = runTest {
        val locationRepository = dm.getRepository<LocationRepository>()
        val countBefore = locationRepository.getAll().count()

        removeLocationUseCase(existingLocation)

        val removedLocation = locationRepository.get(existingLocation.id)
        assertNull(removedLocation)

        val countAfter = locationRepository.getAll().count()
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeLocation_LocationRemoved_AislesRemoved() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val aisleCountLocation = aisleRepository.getForLocation(existingLocation.id).count()
        val aisleCountBefore = aisleRepository.getAll().count()

        removeLocationUseCase(existingLocation)

        val aisleCountAfter = aisleRepository.getAll().count()
        assertEquals(aisleCountBefore - aisleCountLocation, aisleCountAfter)
    }

    @Test
    fun removeLocation_LocationRemoved_AisleProductsRemoved() = runTest {
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val aisles =
            dm.getRepository<AisleRepository>().getForLocation(existingLocation.id)
        val aisleProductCountLocation =
            aisleProductRepository.getAll().count { it.aisleId in aisles.map { a -> a.id } }
        val aisleProductCountBefore = aisleProductRepository.getAll().count()

        removeLocationUseCase(existingLocation)

        val aisleProductCountAfter = aisleProductRepository.getAll().count()
        assertEquals(aisleProductCountBefore - aisleProductCountLocation, aisleProductCountAfter)
    }

    @Test
    fun removeLocation_LocationHasNoAisles_LocationRemoved() = runTest {
        val locationRepository = dm.getRepository<LocationRepository>()
        val newLocationId = locationRepository.add(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = "Dummy Shop",
                pinned = false,
                aisles = emptyList(),
                showDefaultAisle = true,
                expanded = true,
                rank = dm.getUseCase<GetLocationMaxRankUseCase>().invoke() + 1
            )
        )
        val newLocation = locationRepository.get(newLocationId)!!

        removeLocationUseCase(newLocation)

        val removedLocation = locationRepository.get(newLocation.id)
        assertNull(removedLocation)
    }
}