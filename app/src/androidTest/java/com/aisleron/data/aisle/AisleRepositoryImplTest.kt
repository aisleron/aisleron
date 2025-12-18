package com.aisleron.data.aisle

import com.aisleron.data.RepositoryImplTest
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AisleRepositoryImplTest : RepositoryImplTest<Aisle>() {
    val aisleRepository: AisleRepository get() = repository as AisleRepository

    override fun initRepository(): BaseRepository<Aisle> = AisleRepositoryImpl(
        aisleDao = get<AisleDao>(),
        aisleMapper = AisleMapper()
    )

    private suspend fun getLocation(): Location =
        get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }

    override suspend fun getSingleNewItem(): Aisle =
        Aisle(
            id = 0,
            name = "Aisle 100",
            products = emptyList(),
            locationId = getLocation().id,
            rank = 100,
            isDefault = false,
            expanded = true
        )

    override suspend fun getMultipleNewItems(): List<Aisle> {
        val locationId = getLocation().id
        return listOf(
            Aisle(
                id = 0,
                name = "Aisle 100",
                products = emptyList(),
                locationId = locationId,
                rank = 100,
                isDefault = false,
                expanded = true
            ),
            Aisle(
                id = 0,
                name = "Aisle 101",
                products = emptyList(),
                locationId = locationId,
                rank = 101,
                isDefault = false,
                expanded = true
            )
        )
    }

    override suspend fun getInvalidItem(): Aisle =
        Aisle(
            id = -1,
            name = "dummy Aisle",
            products = emptyList(),
            locationId = getLocation().id,
            rank = 100,
            isDefault = false,
            expanded = true
        )

    override fun getUpdatedItem(item: Aisle): Aisle =
        item.copy(name = "${item.name} Updated")

    @Test
    fun getDefaultAisleFor_LocationHasDefaultAisle_ReturnDefaultAisle() = runTest {
        val location = getLocation()

        val aisle = aisleRepository.getDefaultAisleFor(location.id)

        assertNotNull(aisle)
        assertTrue(aisle.isDefault)
    }

    @Test
    fun getDefaultAisleFor_IsInvalidLocation_ReturnNull() = runTest {
        val aisle = aisleRepository.getDefaultAisleFor(-1)

        assertNull(aisle)
    }

    @Test
    fun getWithProducts_AisleHasProducts_ResponseIncludesProducts() = runTest {
        val aisleProductRepository = get<AisleProductRepository>()
        val aisleId = aisleProductRepository.getAll().first().aisleId
        val aisleProducts = aisleProductRepository.getAll().filter { it.aisleId == aisleId }

        val aisle = aisleRepository.getWithProducts(aisleId)

        assertNotNull(aisle)
        assertTrue(aisle.products.isNotEmpty())
        assertEquals(aisleProducts.size, aisle.products.size)
    }


    @Test
    fun updateAisleRank_NewRankProvided_AisleRankUpdated() = runTest {
        val existingAisle = aisleRepository.getAll().first { !it.isDefault }
        val updateAisle = existingAisle.copy(rank = 1001)

        aisleRepository.updateAisleRank(updateAisle)

        val updatedAisle = aisleRepository.get(existingAisle.id)
        assertEquals(updateAisle, updatedAisle)
    }

    @Test
    fun updateAisleRank_AisleRankUpdated_OtherAislesMoved() = runTest {
        val existingAisle = aisleRepository.getAll().first { !it.isDefault }
        val updateAisle = existingAisle.copy(rank = existingAisle.rank + 1)
        val maxAisleRankBefore: Int = aisleRepository.getAll()
            .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }

        aisleRepository.updateAisleRank(updateAisle)

        val maxAisleRankAfter: Int = aisleRepository.getAll()
            .filter { it.locationId == existingAisle.locationId }.maxOf { it.rank }

        assertEquals(maxAisleRankBefore + 1, maxAisleRankAfter)
    }

    /**
     * Extra Tests
     * updateAisleRank(aisle: Aisle)
     */
}