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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetShopsUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getShopsUseCase: GetShopsUseCase

    @BeforeEach
    fun setUp() {
        // TODO: rework this test so it uses seed data and checks a list contains only shops
        dm = TestDependencyManager(addData = false)
        getShopsUseCase = dm.getUseCase()
    }

    @Test
    fun getShops_NoShopsDefined_ReturnEmptyList() = runTest {
        val resultList = getShopsUseCase().first()
        assertEquals(0, resultList.count())
    }

    @Test
    fun getShops_ShopsDefined_ReturnShopsList() = runTest {
        dm.getRepository<LocationRepository>().add(
            listOf(
                Location(
                    id = 1000,
                    type = LocationType.SHOP,
                    defaultFilter = FilterType.NEEDED,
                    name = "Shop 1",
                    pinned = false,
                    aisles = emptyList(),
                    showDefaultAisle = true
                ),
                Location(
                    id = 2000,
                    type = LocationType.SHOP,
                    defaultFilter = FilterType.NEEDED,
                    name = "Shop 2",
                    pinned = false,
                    aisles = emptyList(),
                    showDefaultAisle = true
                ),
            )
        )

        val resultList = getShopsUseCase().first()

        assertEquals(2, resultList.count())
    }
}