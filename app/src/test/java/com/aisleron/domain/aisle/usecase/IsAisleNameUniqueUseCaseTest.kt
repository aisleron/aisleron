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

package com.aisleron.domain.aisle.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsAisleNameUniqueUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var existingAisle: Aisle
    private lateinit var isAisleNameUniqueUseCase: IsAisleNameUniqueUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()

        existingAisle = runBlocking {
            dm.getRepository<AisleRepository>().getAll().first { !it.isDefault }
        }

        isAisleNameUniqueUseCase = dm.getUseCase()
    }

    @Test
    fun isNameUnique_NoMatchingNameExists_ReturnTrue() = runTest {
        val newAisle = existingAisle.copy(id = 0, name = "Aisle Unique Name")

        val result = isAisleNameUniqueUseCase(newAisle)

        Assertions.assertNotEquals(existingAisle.name, newAisle.name)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_AisleIdsMatch_ReturnTrue() = runTest {
        val newAisle = existingAisle.copy(expanded = !existingAisle.expanded)

        val result = isAisleNameUniqueUseCase(newAisle)

        Assertions.assertEquals(existingAisle.id, newAisle.id)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_NamesMatchIdsDiffer_ReturnFalse() = runTest {
        val newAisle = existingAisle.copy(id = 0)

        val result = isAisleNameUniqueUseCase(newAisle)

        Assertions.assertEquals(existingAisle.name, newAisle.name)
        Assertions.assertNotEquals(existingAisle.id, newAisle.id)
        Assertions.assertFalse(result)
    }

    @Test
    fun isNameUnique_NamesMatchWithDifferentCase_ReturnFalse() = runTest {
        val newAisle = existingAisle.copy(id = 0, name = "  ${existingAisle.name.uppercase()}  ")

        val result = isAisleNameUniqueUseCase(newAisle)

        Assertions.assertEquals(
            existingAisle.name.uppercase().trim(), newAisle.name.uppercase().trim()
        )

        Assertions.assertNotEquals(existingAisle.id, newAisle.id)
        Assertions.assertFalse(result)
    }
}