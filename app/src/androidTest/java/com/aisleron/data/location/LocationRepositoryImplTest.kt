/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.data.location

import com.aisleron.data.RepositoryImplTest
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocationRepositoryImplTest : RepositoryImplTest<Location>() {
    val locationRepository: LocationRepository get() = repository as LocationRepository

    override fun initRepository(): BaseRepository<Location> =
        LocationRepositoryImpl(
            locationDao = get<LocationDao>(),
            locationMapper = LocationMapper()
        )

    override suspend fun getSingleNewItem(): Location = Location(
        id = 0,
        name = "New Location",
        type = LocationType.SHOP,
        pinned = false,
        aisles = emptyList(),
        showDefaultAisle = true,
        defaultFilter = FilterType.NEEDED,
        expanded = true,
        rank = 1000
    )

    override suspend fun getMultipleNewItems(): List<Location> {
        return listOf(
            getSingleNewItem(),
            getSingleNewItem().copy(name = "New Location 2", pinned = true, rank = 2000)
        )
    }

    override suspend fun getInvalidItem(): Location =
        getSingleNewItem().copy(id = -1)

    override fun getUpdatedItem(item: Location): Location =
        item.copy(name = "${item.name} Updated")

    @Test
    fun getPinnedShops_ReturnPinnedShopLocations() = runTest {
        addMultipleItems()
        val pinnedShopCount = locationRepository.getAll().count { it.pinned }

        val pinnedShops = locationRepository.getPinnedShops().first()

        assertEquals(pinnedShopCount, pinnedShops.count())
        assertTrue(pinnedShops.none { !it.pinned })
    }

    @Test
    fun getShops_ReturnShopLocations() = runTest {
        addMultipleItems()
        val shopCount = locationRepository.getAll().count { it.type == LocationType.SHOP }

        val shops = locationRepository.getShops().first()

        assertEquals(shopCount, shops.count())
        assertTrue(shops.none { it.type != LocationType.SHOP })
    }

    @Test
    fun getHome_ReturnHomeLocation() = runTest {
        val home = locationRepository.getHome()

        assertNotNull(home)
        assertEquals(LocationType.HOME, home.type)
    }

    @Test
    fun getLocationWithAisles_LocationHasAisles_ResponseIncludesAisles() = runTest {
        val aisleRepository = get<AisleRepository>()
        val locationId = aisleRepository.getAll().first().locationId
        val aisles = aisleRepository.getAll().filter { it.locationId == locationId }

        val location = locationRepository.getLocationWithAisles(locationId)

        assertNotNull(location)
        assertTrue(location.aisles.isNotEmpty())
        assertEquals(aisles.size, location.aisles.size)
    }

    @Test
    fun getByName_IsUnknownName_ReturnNull() = runTest {
        val location = locationRepository.getByName("Unknown Location")

        assertNull(location)
    }

    @Test
    fun getByName_IsKnownName_ReturnLocation() = runTest {
        val locationName = locationRepository.getAll().first { it.type != LocationType.HOME }.name

        val location = locationRepository.getByName(locationName)

        assertNotNull(location)
        assertEquals(locationName, location.name)
    }

    @Test
    fun getMaxRank_ReturnMaxRank() = runTest {
        addMultipleItems()
        val expected = locationRepository.getAll().maxOf { it.rank }

        val actual = locationRepository.getLocationMaxRank()

        assertEquals(expected, actual)
    }
}