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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class UpdateLocationExpandedUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateLocationExpandedUseCase: UpdateLocationExpandedUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        updateLocationExpandedUseCase = dm.getUseCase()
    }

    @ParameterizedTest(name = "Test when Expanded is {0}")
    @MethodSource("expandedArguments")
    fun updateLocationExpanded_LocationExists_ExpandedUpdated(expanded: Boolean) = runTest {
        val existingLocation = dm.getRepository<LocationRepository>().getAll().first()

        val updatedLocation = updateLocationExpandedUseCase(existingLocation, expanded)

        assertNotNull(updatedLocation)
        assertEquals(existingLocation.id, updatedLocation.id)
        assertEquals(existingLocation.name, updatedLocation.name)
        assertEquals(expanded, updatedLocation.expanded)
    }

    private companion object {
        @JvmStatic
        fun expandedArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}