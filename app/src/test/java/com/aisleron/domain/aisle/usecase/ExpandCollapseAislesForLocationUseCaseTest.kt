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

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.LocationRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExpandCollapseAislesForLocationUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var useCase: ExpandCollapseAislesForLocationUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        useCase = ExpandCollapseAislesForLocationUseCaseImpl(
            dm.getRepository<AisleRepository>()
        )
    }

    @Test
    fun expandCollapseAislesForLocation_hasExpandedAisles_CollapseAllAisles() = runTest {
        val locationId = dm.getRepository<LocationRepository>().getHome().id
        val aisleRepository = dm.getRepository<AisleRepository>()
        val aisles = aisleRepository.getForLocation(locationId)
        aisleRepository.update(aisles.first().copy(expanded = true))
        aisleRepository.update(aisles.last().copy(expanded = false))

        useCase(locationId)

        val aislesAfter = aisleRepository.getForLocation(locationId)
        Assertions.assertFalse(aislesAfter.any { it.expanded })
        Assertions.assertEquals(aislesAfter.count(), aislesAfter.count { !it.expanded })
    }

    @Test
    fun expandCollapseAislesForLocation_noExpandedAisles_ExpandAllAisles() = runTest {
        val locationId = dm.getRepository<LocationRepository>().getHome().id
        val aisleRepository = dm.getRepository<AisleRepository>()
        val aisles = aisleRepository.getForLocation(locationId)
        aisleRepository.update(aisles.map { it.copy(expanded = false) })

        useCase(locationId)

        val aislesAfter = aisleRepository.getForLocation(locationId)
        Assertions.assertFalse(aislesAfter.any { !it.expanded })
        Assertions.assertEquals(aislesAfter.count(), aislesAfter.count { it.expanded })
    }

    @Test
    fun expandCollapseAislesForLocation_noAislesToUpdate_NoAislesUpdated() = runTest {
        val aisleRepository = dm.getRepository<AisleRepository>()
        val expandedBefore = aisleRepository.getAll().count { it.expanded }

        useCase(-1)

        val expandedAfter = aisleRepository.getAll().count { it.expanded }
        Assertions.assertEquals(expandedBefore, expandedAfter)
    }

    /**
     * Tests
     *  Has expanded aisle, collapse
     *  All collapsed, expand
     *  No aisles;
     */

}