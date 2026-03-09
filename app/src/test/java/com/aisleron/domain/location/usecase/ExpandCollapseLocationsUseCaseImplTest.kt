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
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ExpandCollapseLocationsUseCaseImplTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var useCase: ExpandCollapseLocationsUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        useCase = ExpandCollapseLocationsUseCaseImpl(
            dm.getRepository<LocationRepository>()
        )
    }

    @ParameterizedTest(name = "Expanded is {0}")
    @MethodSource("expandArguments")
    fun expandCollapseLocations_UpdatesExpanded(expand: Boolean) = runTest {
        val repository = dm.getRepository<LocationRepository>()
        val shop = repository.getShops().first().first()
        repository.add(shop.copy(id = 0, expanded = !shop.expanded))

        useCase(LocationType.SHOP, expand)

        val shopsAfter = repository.getShops().first()
        assertTrue(shopsAfter.all { it.expanded == expand })
    }

    private companion object {
        @JvmStatic
        fun expandArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}