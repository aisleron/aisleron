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

package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAisleProductMaxRankUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getAisleProductMaxRankUseCase: GetAisleProductMaxRankUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getAisleProductMaxRankUseCase = dm.getUseCase()
    }

    private suspend fun getAisle(): Aisle {

        val locationId = dm.getRepository<LocationRepository>().add(
            Location(
                id = 0,
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                name = "Rank Test Shop",
                pinned = false,
                aisles = emptyList(),
                showDefaultAisle = true
            )
        )

        val aisleId = dm.getRepository<AisleRepository>().add(
            Aisle(
                id = 0,
                name = "RankTestAisle",
                locationId = locationId,
                rank = 1000,
                isDefault = false,
                products = emptyList(),
                expanded = true
            )
        )

        return dm.getRepository<AisleRepository>().get(aisleId)!!
    }

    @Test
    fun getAisleProductMaxRank_AisleHasProducts_RankIsMax() = runTest {
        val aisle = getAisle()
        val productRepository = dm.getRepository<ProductRepository>()
        dm.getRepository<AisleProductRepository>().add(
            listOf(
                AisleProduct(100, aisle.id, productRepository.get(1)!!, 0),
                AisleProduct(200, aisle.id, productRepository.get(2)!!, 0),
                AisleProduct(300, aisle.id, productRepository.get(3)!!, 0)
            )
        )

        val maxRankResult = getAisleProductMaxRankUseCase(aisle)

        Assertions.assertEquals(300, maxRankResult)
    }

    @Test
    fun getAisleProductMaxRank_AisleHasNoProducts_RankIsZero() = runTest {
        val aisle = getAisle()

        val maxRankResult = getAisleProductMaxRankUseCase(aisle)

        Assertions.assertEquals(0, maxRankResult)
    }
}