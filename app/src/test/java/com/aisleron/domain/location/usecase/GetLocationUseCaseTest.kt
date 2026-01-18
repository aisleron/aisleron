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
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetLocationUseCaseTest {

    private lateinit var dm: TestDependencyManager
    private lateinit var getLocationUseCase: GetLocationUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getLocationUseCase = GetLocationUseCase(dm.getRepository<LocationRepository>())
    }

    @Test
    fun getLocation_NonExistentId_ReturnNull() = runTest {
        assertNull(getLocationUseCase(2001))
    }

    @Test
    fun getLocation_ExistingId_ReturnLocation() = runTest {
        val location = getLocationUseCase(1)
        assertNotNull(location)
        assertEquals(1, location!!.id)
    }
}