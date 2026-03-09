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
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAisleMaxRankUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getAisleMaxRankUseCase: GetAisleMaxRankUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getAisleMaxRankUseCase = dm.getUseCase()
    }

    private suspend fun getLocation(): Location {
        val locationId = dm.getUseCase<AddLocationUseCase>().invoke(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = "Rank Test Shop",
                pinned = false,
                aisles = emptyList(),
                showDefaultAisle = true,
                expanded = true,
                rank = 10
            )
        )

        return dm.getRepository<LocationRepository>().get(locationId)!!
    }

    private fun getAisle(name: String, locationId: Int, rank: Int) : Aisle = Aisle (
        name = name,
        products = emptyList(),
        locationId = locationId,
        rank = rank,
        isDefault = false,
        expanded = true,
        id = 0
    )

    @Test
    fun getAisleMaxRank_LocationHasAisles_RankIsMax() = runTest {
        val location = getLocation()
        val aisleRepository = dm.getRepository<AisleRepository>()
        aisleRepository.add(
            listOf(
                getAisle("Aisle 1", location.id, 100),
                getAisle("Aisle 2", location.id, 200),
                getAisle("Aisle 3", location.id, 300)
            )
        )

        val maxRankResult = getAisleMaxRankUseCase(location.id)

        Assertions.assertEquals(300, maxRankResult)
    }

    @Test
    fun getAisleMaxRank_LocationHasNoAisles_RankIsZero() = runTest {
        val location = getLocation()

        val maxRankResult = getAisleMaxRankUseCase(location.id)

        Assertions.assertEquals(0, maxRankResult)
    }

    @Test
    fun getAisleMaxRank_InvalidLocationProvided_RankIsZero() = runTest {
        val maxRankResult = getAisleMaxRankUseCase(-1)

        Assertions.assertEquals(0, maxRankResult)
    }

}