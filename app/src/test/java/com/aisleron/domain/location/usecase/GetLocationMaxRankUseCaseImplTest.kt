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
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetLocationMaxRankUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getLocationMaxRankUseCase: GetLocationMaxRankUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getLocationMaxRankUseCase = dm.getUseCase()
    }

    private suspend fun addLocation(name: String, rank: Int) {
        dm.getRepository<LocationRepository>().add(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = name,
                pinned = false,
                aisles = emptyList(),
                showDefaultAisle = true,
                expanded = true,
                rank = rank
            )
        )
    }

    @Test
    fun getLocationMaxRank_AutoAddedLocations_RankIsMax() = runTest {
        val maxRank = dm.getRepository<LocationRepository>().getAll().maxOf { it.rank }

        val maxRankResult = getLocationMaxRankUseCase()

        Assertions.assertEquals(maxRank, maxRankResult)
    }

    @Test
    fun getLocationMaxRank_AdditionalLocations_RankIsMax() = runTest {
        addLocation("Location 100", 100)
        addLocation("Location 200", 200)
        addLocation("Location 300", 300)

        val maxRankResult = getLocationMaxRankUseCase()

        Assertions.assertEquals(300, maxRankResult)
    }
}