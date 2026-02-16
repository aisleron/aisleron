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
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SortLocationTypeByNameUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var sortLocationTypeByNameUseCase: SortLocationTypeByNameUseCase
    private lateinit var locationRepository: LocationRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        sortLocationTypeByNameUseCase = dm.getUseCase()
        locationRepository = dm.getRepository()
    }

    @Test
    fun sortLocationTypeByName_SortAisles_aislesSorted() = runTest {
        val locationId = locationRepository.getShops().first().first().id
        val addAisleUseCase = dm.getUseCase<AddAisleUseCase>()

        val aisle = Aisle(
            name = "ZZZ",
            products = emptyList(),
            locationId = locationId,
            rank = 2000,
            isDefault = false,
            id = 0,
            expanded = true
        )

        addAisleUseCase(aisle)
        addAisleUseCase(aisle.copy(name = "AAA", rank = 2002))

        sortLocationTypeByNameUseCase(LocationType.SHOP, true)

        val aisles = dm.getRepository<AisleRepository>().getForLocation(locationId)

        assertEquals(1, aisles.first { it.name == "AAA" }.rank)
        assertEquals(aisles.maxOf { it.rank } - 1, aisles.first { it.name == "ZZZ" }.rank)
        assertEquals(aisles.maxOf { it.rank }, aisles.first { it.isDefault }.rank)
    }

    @Test
    fun sortLocationTypeByNam_HasMultipleLocations_LocationsSorted() = runTest {
        val addLocationUseCase = dm.getUseCase<AddLocationUseCase>()

        val location = Location(
            id = 0,
            name = "ZZZ",
            rank = 2000,
            expanded = true,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            pinned = false,
            aisles = emptyList(),
            showDefaultAisle = true
        )

        addLocationUseCase(location)
        addLocationUseCase(location.copy(name = "AAA", rank = 2002))

        sortLocationTypeByNameUseCase(LocationType.SHOP, false)

        val locations = locationRepository.getByType(LocationType.SHOP)
        assertEquals(1, locations.first { it.name == "AAA" }.rank)
        assertEquals(locations.maxOf { it.rank }, locations.first { it.name == "ZZZ" }.rank)
    }
}