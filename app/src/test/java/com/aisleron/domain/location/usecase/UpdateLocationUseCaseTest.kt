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
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class UpdateLocationUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateLocationUseCase: UpdateLocationUseCase
    private lateinit var existingLocation: Location

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        val locationRepository = dm.getRepository<LocationRepository>()
        updateLocationUseCase = dm.getUseCase()
        existingLocation = runBlocking {
            locationRepository.getAll().first { it.type == LocationType.SHOP }
        }
    }

    @Test
    fun updateLocation_IsDuplicateName_ThrowsException() = runTest {
        val locationRepository = dm.getRepository<LocationRepository>()
        val id = locationRepository.add(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = "Shop 99",
                pinned = false,
                aisles = emptyList(),
                showDefaultAisle = true
            )
        )

        val updateLocation =
            locationRepository.get(id)!!.copy(name = existingLocation.name)

        assertThrows<AisleronException.DuplicateLocationNameException> {
            updateLocationUseCase(updateLocation)
        }
    }

    @ParameterizedTest(name = "Test UpdateLocation when showDefaultAisle is {0}")
    @MethodSource("showDefaultAisleArguments")
    fun updateLocation_IsExistingLocation_LocationUpdated(showDefaultAisle: Boolean) = runTest {
        val updateLocation =
            existingLocation.copy(
                name = existingLocation.name + " Updated",
                pinned = !existingLocation.pinned,
                showDefaultAisle = showDefaultAisle
            )

        val locationRepository = dm.getRepository<LocationRepository>()
        val countBefore = locationRepository.getAll().count()

        updateLocationUseCase(updateLocation)

        val updatedLocation = locationRepository.getByName(updateLocation.name)
        assertNotNull(updatedLocation)
        assertEquals(updateLocation.id, updatedLocation?.id)
        assertEquals(updateLocation.name, updatedLocation?.name)
        assertEquals(updateLocation.type, updatedLocation?.type)
        assertEquals(updateLocation.pinned, updatedLocation?.pinned)
        assertEquals(updateLocation.defaultFilter, updatedLocation?.defaultFilter)
        assertEquals(showDefaultAisle, updatedLocation?.showDefaultAisle)

        val countAfter = locationRepository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun updateLocation_IsNewLocation_RecordCreated() = runTest {
        val newLocation = existingLocation.copy(
            id = 0,
            name = existingLocation.name + " Inserted"
        )

        val locationRepository = dm.getRepository<LocationRepository>()
        val countBefore = locationRepository.getAll().count()

        updateLocationUseCase(newLocation)

        val updatedLocation = locationRepository.getByName(newLocation.name)
        assertNotNull(updatedLocation)
        assertEquals(newLocation.name, updatedLocation?.name)
        assertEquals(newLocation.type, updatedLocation?.type)
        assertEquals(newLocation.pinned, updatedLocation?.pinned)
        assertEquals(newLocation.defaultFilter, updatedLocation?.defaultFilter)

        val countAfter = locationRepository.getAll().count()
        assertEquals(countBefore + 1, countAfter)
    }

    private companion object {
        @JvmStatic
        fun showDefaultAisleArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}