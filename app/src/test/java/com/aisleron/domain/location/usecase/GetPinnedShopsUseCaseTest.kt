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

import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.data.TestDataManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPinnedShopsUseCaseTest {

    private lateinit var testData: TestDataManager

    private lateinit var getPinnedShopsUseCase: GetPinnedShopsUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getPinnedShopsUseCase = GetPinnedShopsUseCase(testData.getRepository<LocationRepository>())
    }

    @Test
    fun getPinnedShops_NoPinnedShopsDefined_ReturnEmptyList() {
        val resultList: List<Location> =
            runBlocking {
                val locationRepository = testData.getRepository<LocationRepository>()
                locationRepository.getAll().filter { it.pinned }
                    .forEach { locationRepository.remove(it) }

                getPinnedShopsUseCase().first()
            }
        Assertions.assertEquals(0, resultList.count())
    }

    @Test
    fun getShops_PinnedShopsDefined_ReturnPinnedShopsList() {
        val pinnedCount: Int
        val resultList: List<Location> =
            runBlocking {
                pinnedCount =
                    testData.getRepository<LocationRepository>().getAll().count { it.pinned }
                getPinnedShopsUseCase().first()
            }

        Assertions.assertEquals(pinnedCount, resultList.count())
    }
}