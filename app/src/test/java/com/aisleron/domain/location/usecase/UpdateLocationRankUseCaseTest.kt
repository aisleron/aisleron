/*
 * Copyright (C) 2026 aisleron.com
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
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class UpdateLocationRankUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateLocationRankUseCase: UpdateLocationRankUseCase
    private lateinit var existingLocation: Location
    private lateinit var locationRepository: LocationRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        locationRepository = dm.getRepository<LocationRepository>()
        updateLocationRankUseCase = dm.getUseCase()
        existingLocation =
            runBlocking { locationRepository.getAll().first { it.type == LocationType.SHOP } }
    }

    @Test
    fun updateLocationRank_NewRankProvided_LocationRankUpdated() = runTest {
        val newRank = 1001

        updateLocationRankUseCase(existingLocation.id, newRank)

        val updatedLocation = locationRepository.get(existingLocation.id)
        assertEquals(existingLocation.copy(rank = newRank), updatedLocation)
    }

    @Test
    fun updateLocationRank_LocationRankUpdated_OtherLocationsMoved() = runTest {
        locationRepository.add(
            Location(
                id = 0,
                name = "Test Location",
                type = LocationType.SHOP,
                rank = 101,
                aisles = emptyList(),
                defaultFilter = FilterType.NEEDED,
                pinned = false,
                showDefaultAisle = true,
                expanded = false
            )
        )

        val newRank = existingLocation.rank + 1
        val maxRankBefore: Int = locationRepository.getAll().maxOf { it.rank }

        updateLocationRankUseCase(existingLocation.id, newRank)

        val maxRankAfter: Int = locationRepository.getAll().maxOf { it.rank }
        assertEquals(maxRankBefore + 1, maxRankAfter)
    }

    @Test
    fun updateLocationRank_InvalidIdProvided_NoLocationsUpdated() = runTest {
        val newRank = 1001

        updateLocationRankUseCase(-1, 1001)

        val updatedAisle = locationRepository.getAll().firstOrNull { it.rank == newRank }
        assertNull(updatedAisle)
    }

}