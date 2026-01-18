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

package com.aisleron.domain.aisle.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAislesForLocationUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getAislesForLocationUseCase: GetAislesForLocationUseCase
    private lateinit var aisleRepository: AisleRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var location: Location

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getAislesForLocationUseCase = dm.getUseCase()
        aisleRepository = dm.getRepository()
        locationRepository = dm.getRepository()
        runBlocking {
            location = locationRepository.getAll().first()
        }
    }

    @Test
    fun getAislesForLocation_noAisles_returnsEmptyList() = runTest {
        val newLocationId = locationRepository.add(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = "New Location",
                pinned = false,
                emptyList(),
                showDefaultAisle = true,
                noteId = null
            )
        )

        val result = getAislesForLocationUseCase(newLocationId)
        assertTrue(result.isEmpty())
    }

    @Test
    fun getAislesForLocation_hasAisles_returnsOnlyLocationAisles() = runTest {
        val allAisles = aisleRepository.getAll()
        val expectedAisles = allAisles.filter { it.locationId == location.id }

        val result = getAislesForLocationUseCase(location.id)

        assertEquals(expectedAisles.size, result.size)
        assertTrue(result.containsAll(expectedAisles))
    }

    @Test
    fun getAislesForLocation_hasAisles_returnsAislesSortedByRank() = runTest {
        val result = getAislesForLocationUseCase(location.id)

        val sortedResult = result.sortedBy { it.rank }
        assertEquals(sortedResult, result)
    }

    @Test
    fun getAislesForLocation_invalidLocationId_returnsEmptyList() = runTest {
        val result = getAislesForLocationUseCase(-1)

        assertTrue(result.isEmpty())
    }
}
