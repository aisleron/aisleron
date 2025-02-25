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
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsLocationNameUniqueUseCaseTest {

    private lateinit var isLocationNameUniqueUseCase: IsLocationNameUniqueUseCase

    @BeforeEach
    fun setUp() {
        isLocationNameUniqueUseCase =
            IsLocationNameUniqueUseCase(testData.getRepository<LocationRepository>())
    }

    @Test
    fun isNameUnique_NoMatchingNameExists_ReturnTrue() {
        val newLocation = existingLocation.copy(id = 0, name = "Shop Test Unique Name")
        val result = runBlocking {
            isLocationNameUniqueUseCase(newLocation)
        }
        assertNotEquals(existingLocation.name, newLocation.name)
        assertTrue(result)
    }

    @Test
    fun isNameUnique_LocationIdsMatch_ReturnTrue() {
        val newLocation = existingLocation.copy(pinned = true)
        val result = runBlocking {
            isLocationNameUniqueUseCase(newLocation)
        }
        assertEquals(existingLocation.id, newLocation.id)
        assertTrue(result)
    }

    @Test
    fun isNameUnique_LocationTypesDiffer_ReturnTrue() {
        val newLocation = existingLocation.copy(id = 0, type = LocationType.HOME)
        val result = runBlocking {
            isLocationNameUniqueUseCase(newLocation)
        }
        assertNotEquals(existingLocation.type, newLocation.type)
        assertTrue(result)
    }

    @Test
    fun isNameUnique_NamesMatchTypesMatchIdsDiffer_ReturnFalse() {
        val newLocation = existingLocation.copy(id = 0)
        val result = runBlocking {
            isLocationNameUniqueUseCase(newLocation)
        }
        assertEquals(existingLocation.name, newLocation.name)
        assertEquals(existingLocation.type, newLocation.type)
        assertNotEquals(existingLocation.id, newLocation.id)
        assertFalse(result)
    }

    companion object {

        private lateinit var testData: TestDataManager
        private lateinit var existingLocation: Location

        @JvmStatic
        @BeforeAll
        fun beforeSpec() {
            testData = TestDataManager()
            existingLocation = runBlocking {
                testData.getRepository<LocationRepository>().getAll()
                    .first { it.type == LocationType.SHOP }
            }
        }
    }
}