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
import com.aisleron.domain.aisle.AisleRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class UpdateAisleExpandedUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateAisleExpandedUseCase: UpdateAisleExpandedUseCase
    private lateinit var aisleRepository: AisleRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        updateAisleExpandedUseCase = dm.getUseCase()
        aisleRepository = dm.getRepository()
    }

    @ParameterizedTest(name = "Test when Expand is {0}")
    @MethodSource("expandArguments")
    fun updateAisleExpanded_AisleExists_ExpandedUpdated(expand: Boolean) = runTest {
        val existingAisle = aisleRepository.getAll().first()

        updateAisleExpandedUseCase(existingAisle.id, expand)

        val updatedAisle = aisleRepository.get(existingAisle.id)
        assertEquals(existingAisle.copy(expanded = expand), updatedAisle)
    }

    @Test
    fun updateAisleExpanded_InvalidIdProvided_NoAislesUpdated() = runTest {
        val expand = false
        val expandedCountBefore = aisleRepository.getAll().count { it.expanded }

        updateAisleExpandedUseCase(-1, expand)

        val expandedCountAfter = aisleRepository.getAll().count { it.expanded }
        assertEquals(expandedCountBefore, expandedCountAfter)
    }

    private companion object {
        @JvmStatic
        fun expandArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}