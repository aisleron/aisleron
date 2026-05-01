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

package com.aisleron.data.aisle

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao

@Dao
interface AisleDao : BaseDao<AisleEntity> {
    /**
     * Aisle
     */
    @Query("SELECT * FROM Aisle WHERE id = :aisleId AND (isRemoved = 0 OR :includeRemoved = 1)")
    suspend fun getAisle(aisleId: Int, includeRemoved: Boolean): AisleEntity?

    @Query("SELECT * FROM Aisle WHERE isRemoved = 0")
    suspend fun getAisles(): List<AisleEntity>

    @Query("SELECT * FROM Aisle WHERE locationId = :locationId AND isRemoved = 0")
    suspend fun getAislesForLocation(locationId: Int): List<AisleEntity>

    @Query("SELECT * FROM Aisle WHERE isDefault = 1 AND isRemoved = 0")
    suspend fun getDefaultAisles(): List<AisleEntity>

    @Query("SELECT * FROM Aisle WHERE isDefault = 1 AND locationId = :locationId AND isRemoved = 0")
    suspend fun getDefaultAisleFor(locationId: Int): AisleEntity?

    @Transaction
    suspend fun updateRank(aisle: AisleEntity) {
        moveRanks(aisle.locationId, aisle.rank, aisle.lastModifiedAt)
        upsert(aisle)
    }

    @Query("UPDATE Aisle SET rank = rank + 1, lastModifiedAt = :lastModifiedAt WHERE locationId = :locationId and rank >= :fromRank")
    suspend fun moveRanks(locationId: Int, fromRank: Int, lastModifiedAt: Long)

    @Query("SELECT COALESCE(MAX(rank), 0) FROM Aisle WHERE locationId = :locationId and isDefault = 0")
    suspend fun getMaxRank(locationId: Int): Int

    /**
     * Aisle With Product
     */
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId AND isRemoved = 0")
    suspend fun getAisleWithProducts(aisleId: Int): AisleWithProducts

    @Transaction
    @Query("SELECT * FROM Aisle WHERE isRemoved = 0")
    suspend fun getAislesWithProducts(): List<AisleWithProducts>
}