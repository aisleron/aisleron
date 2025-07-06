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

package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetShopsUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getShopsUseCase: GetShopsUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager(addData = false)
        getShopsUseCase = GetShopsUseCase(testData.getRepository<LocationRepository>())

    }

    @Test
    fun getShops_NoShopsDefined_ReturnEmptyList() {
        val resultList: List<Location> =
            runBlocking {
                getShopsUseCase().first()
            }
        assertEquals(0, resultList.count())
    }

    @Test
    fun getShops_ShopsDefined_ReturnShopsList() {
        val resultList: List<Location> =
            runBlocking {
                testData.getRepository<LocationRepository>().add(
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
                getShopsUseCase().first()
            }

        assertEquals(2, resultList.count())
    }
}