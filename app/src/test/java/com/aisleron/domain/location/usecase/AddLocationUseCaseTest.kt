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
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.ProductRepository
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

class AddLocationUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var addLocationUseCase: AddLocationUseCase
    private lateinit var existingLocation: Location

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        val locationRepository = dm.getRepository<LocationRepository>()
        addLocationUseCase = dm.getUseCase()
        existingLocation = runBlocking { locationRepository.get(1)!! }
    }

    @Test
    fun addLocation_IsDuplicateName_ThrowsException() = runTest {
        val newLocation = existingLocation.copy(id = 0, pinned = !existingLocation.pinned)

        assertThrows<AisleronException.DuplicateLocationNameException> {
            addLocationUseCase(newLocation)
        }
    }

    @Test
    fun addLocation_IsExistingLocation_ThrowsException() = runTest {
        val updateLocation =
            existingLocation.copy(
                name = existingLocation.name + " Updated",
                pinned = !existingLocation.pinned
            )

        assertThrows<AisleronException.DuplicateLocationException> {
            addLocationUseCase(updateLocation)
        }
    }

    private fun getNewLocation(showDefaultAisle: Boolean): Location {
        val newLocation = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "Shop Add New Location",
            pinned = false,
            aisles = emptyList(),
            showDefaultAisle = showDefaultAisle
        )
        return newLocation
    }

    @ParameterizedTest(name = "Test AddLocation when showDefaultAisle is {0}")
    @MethodSource("showDefaultAisleArguments")
    fun addLocation_IsNewLocation_LocationCreated(showDefaultAisle: Boolean) = runTest {
        val newLocation = getNewLocation(showDefaultAisle)
        val locationRepository = dm.getRepository<LocationRepository>()
        val countBefore = locationRepository.getAll().count()

        val id = addLocationUseCase(newLocation)

        val insertedLocation = locationRepository.get(id)
        assertEquals(newLocation.name, insertedLocation?.name)
        assertEquals(newLocation.type, insertedLocation?.type)
        assertEquals(newLocation.pinned, insertedLocation?.pinned)
        assertEquals(newLocation.defaultFilter, insertedLocation?.defaultFilter)
        assertEquals(showDefaultAisle, insertedLocation?.showDefaultAisle)

        val countAfter = locationRepository.getAll().count()
        assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun addLocation_LocationInserted_AddsDefaultAisle() = runTest {
        val newLocation = getNewLocation(true)
        val aisleRepository = dm.getRepository<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()

        val id = addLocationUseCase(newLocation)

        val defaultAisle = aisleRepository.getDefaultAisleFor(id)
        assertNotNull(defaultAisle)

        val aisleCountAfter = aisleRepository.getAll().count()
        assertEquals(aisleCountBefore + 1, aisleCountAfter)
    }

    @Test
    fun addLocation_LocationInserted_AddsAisleProducts() = runTest {
        val newLocation = getNewLocation(true)
        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val productCount: Int = dm.getRepository<ProductRepository>().getAll().count()
        val aisleProductCountBefore = aisleProductRepository.getAll().count()

        val id = addLocationUseCase(newLocation)

        val defaultAisle = dm.getRepository<AisleRepository>().getDefaultAisleFor(id)
        val aisleProducts =
            aisleProductRepository.getAll().filter { it.aisleId == defaultAisle?.id }

        assertEquals(productCount, aisleProducts.count())

        val aisleProductCountAfter = aisleProductRepository.getAll().count()
        assertEquals(aisleProductCountBefore + productCount, aisleProductCountAfter)
    }

    private companion object {
        @JvmStatic
        fun showDefaultAisleArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}