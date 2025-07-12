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

package com.aisleron.data.location

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao : BaseDao<LocationEntity> {

    /**
     * Location
     */
    @Query("SELECT * FROM Location WHERE id = :locationId")
    suspend fun getLocation(locationId: Int): LocationEntity?

    @Query("SELECT * FROM Location")
    suspend fun getLocations(): List<LocationEntity>

    @Query("SELECT * FROM Location WHERE name = :name COLLATE NOCASE")
    suspend fun getLocationByName(name: String): LocationEntity?

    /**
     * Location With Aisles
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE id = :locationId")
    suspend fun getLocationWithAisles(locationId: Int): LocationWithAisles

    /**
     * Location With Aisles With Products
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE id = :locationId")
    fun getLocationWithAislesWithProducts(locationId: Int): Flow<LocationWithAislesWithProducts?>

    /**
     * Shop Specific Queries
     */
    @Query("SELECT * FROM Location WHERE type = 'SHOP'")
    fun getShops(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM Location WHERE type = 'SHOP' AND pinned = 1")
    fun getPinnedShops(): Flow<List<LocationEntity>>

    /**
     * Home Specific Queries
     */
    @Query("SELECT * FROM Location WHERE type = 'HOME'")
    suspend fun getHome(): LocationEntity
}