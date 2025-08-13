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

package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.GetLocationUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddAisleUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var addAisleUseCase: AddAisleUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        addAisleUseCase = AddAisleUseCaseImpl(
            testData.getRepository<AisleRepository>(),
            GetLocationUseCase(testData.getRepository<LocationRepository>()),
            IsAisleNameUniqueUseCase(testData.getRepository<AisleRepository>())
        )
    }

    @Test
    fun addAisle_IsExistingAisle_AisleUpdated() {
        val aisleRepository = testData.getRepository<AisleRepository>()
        val existingAisle = runBlocking { aisleRepository.getAll().first() }
        val updateAisle = existingAisle.copy(
            name = existingAisle.name + " Updated"
        )
        val updatedAisle: Aisle?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = aisleRepository.getAll().count()
            val id = addAisleUseCase(updateAisle)
            updatedAisle = aisleRepository.get(id)
            countAfter = aisleRepository.getAll().count()
        }
        assertNotNull(updatedAisle)
        assertEquals(countBefore, countAfter)
        assertEquals(updateAisle, updatedAisle)
    }

    private fun getNewAisle(): Aisle {
        return Aisle(
            id = 0,
            name = "New Aisle 1",
            products = emptyList(),
            locationId = runBlocking {
                testData.getRepository<LocationRepository>().getAll().first().id
            },
            rank = 1000,
            isDefault = false,
            expanded = true
        )
    }

    @Test
    fun addAisle_IsNewAisle_AisleCreated() {
        val newAisle = getNewAisle()
        val countBefore: Int
        val countAfter: Int
        val insertedAisle: Aisle?
        val aisleRepository = testData.getRepository<AisleRepository>()
        runBlocking {
            countBefore = aisleRepository.getAll().count()
            val id = addAisleUseCase(newAisle)
            insertedAisle = aisleRepository.get(id)
            countAfter = aisleRepository.getAll().count()
        }
        assertNotNull(insertedAisle)
        assertEquals(countBefore + 1, countAfter)
        assertEquals(newAisle.name, insertedAisle?.name)
        assertEquals(newAisle.locationId, insertedAisle?.locationId)
        assertEquals(newAisle.rank, insertedAisle?.rank)
        assertEquals(newAisle.isDefault, insertedAisle?.isDefault)
    }

    @Test
    fun addAisle_IsNewAisle_AisleAddedToExpectedLocation() {
        val newAisle = getNewAisle()
        val countBefore: Int
        val countAfter: Int
        val aisleRepository = testData.getRepository<AisleRepository>()
        runBlocking {
            countBefore = aisleRepository.getForLocation(newAisle.locationId).count()
            addAisleUseCase(newAisle)
            countAfter = aisleRepository.getForLocation(newAisle.locationId).count()
        }
        assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun addAisle_IsInvalidLocation_ThrowsInvalidLocationException() {
        val newAisle = getNewAisle().copy(locationId = -1)
        runBlocking {
            assertThrows<AisleronException.InvalidLocationException> { addAisleUseCase(newAisle) }
        }
    }

    @Test
    fun addAisle_IsDuplicateAisleName_ThrowsDuplicateAisleNameException() = runTest {
        val existingAisle =
            testData.getRepository<AisleRepository>().getAll().first { !it.isDefault }

        val newAisle = existingAisle.copy(id = 0)
        assertThrows<AisleronException.DuplicateAisleNameException> { addAisleUseCase(newAisle) }
    }
}