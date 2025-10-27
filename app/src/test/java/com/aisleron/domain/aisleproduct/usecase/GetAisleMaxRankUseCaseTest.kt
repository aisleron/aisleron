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

class GetAisleMaxRankUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var getAisleMaxRankUseCase: GetAisleMaxRankUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getAisleMaxRankUseCase = dm.getUseCase()
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
    fun getAisleMaxRank_AisleHasProducts_RankIsMax() = runTest {
        val aisle = getAisle()
        val productRepository = dm.getRepository<ProductRepository>()
        dm.getRepository<AisleProductRepository>().add(
            listOf(
                AisleProduct(100, aisle.id, productRepository.get(1)!!, 0),
                AisleProduct(200, aisle.id, productRepository.get(2)!!, 0),
                AisleProduct(300, aisle.id, productRepository.get(3)!!, 0)
            )
        )

        val maxRankResult = getAisleMaxRankUseCase(aisle)

        Assertions.assertEquals(300, maxRankResult)
    }

    @Test
    fun getAisleMaxRank_AisleHasNoProducts_RankIsZero() = runTest {
        val aisle = getAisle()

        val maxRankResult = getAisleMaxRankUseCase(aisle)

        Assertions.assertEquals(0, maxRankResult)
    }
}