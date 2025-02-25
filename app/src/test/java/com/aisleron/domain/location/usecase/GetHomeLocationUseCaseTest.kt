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

import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.data.TestDataManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetHomeLocationUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getHomeLocationUseCase: GetHomeLocationUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getHomeLocationUseCase =
            GetHomeLocationUseCase(testData.getRepository<LocationRepository>())
    }

    @Test
    fun getHomeLocation_WhenCalled_ReturnHomeLocation() {
        val location = runBlocking { getHomeLocationUseCase() }
        assertNotNull(location)
        assertEquals(LocationType.HOME, location.type)
    }
}