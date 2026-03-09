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

package com.aisleron.domain.location.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class UpdateLocationExpandedUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateLocationExpandedUseCase: UpdateLocationExpandedUseCase
    private lateinit var locationRepository: LocationRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        updateLocationExpandedUseCase = dm.getUseCase()
        locationRepository = dm.getRepository()
    }

    @ParameterizedTest(name = "Test when Expand is {0}")
    @MethodSource("expandArguments")
    fun updateLocationExpanded_LocationExists_ExpandedUpdated(expand: Boolean) = runTest {
        val existingLocation = locationRepository.getAll().first()

        updateLocationExpandedUseCase(existingLocation.id, expand)

        val updatedLocation = locationRepository.get(existingLocation.id)
        assertEquals(existingLocation.copy(expanded = expand), updatedLocation)
    }

    @Test
    fun updateLocationExpanded_InvalidIdProvided_NoLocationsUpdated() = runTest {
        val expand = false
        val expandedCountBefore = locationRepository.getAll().count { it.expanded }

        updateLocationExpandedUseCase(-1, expand)

        val expandedCountAfter = locationRepository.getAll().count { it.expanded }
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