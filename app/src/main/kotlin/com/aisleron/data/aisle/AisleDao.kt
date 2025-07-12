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
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId")
    suspend fun getAisle(aisleId: Int): AisleEntity?

    @Transaction
    @Query("SELECT * FROM Aisle")
    suspend fun getAisles(): List<AisleEntity>

    @Transaction
    @Query("SELECT * FROM Aisle WHERE locationId = :locationId")
    suspend fun getAislesForLocation(locationId: Int): List<AisleEntity>

    @Transaction
    @Query("SELECT * FROM Aisle WHERE isDefault = 1")
    suspend fun getDefaultAisles(): List<AisleEntity>

    @Transaction
    @Query("SELECT * FROM Aisle WHERE isDefault = 1 AND locationId = :locationId")
    suspend fun getDefaultAisleFor(locationId: Int): AisleEntity?

    /**
     * Aisle With Product
     */
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId")
    suspend fun getAisleWithProducts(aisleId: Int): AisleWithProducts

    @Transaction
    @Query("SELECT * FROM Aisle")
    suspend fun getAislesWithProducts(): List<AisleWithProducts>

    @Transaction
    suspend fun updateRank(aisle: AisleEntity) {
        moveRanks(aisle.locationId, aisle.rank)
        upsert(aisle)
    }

    @Query("UPDATE Aisle SET rank = rank + 1 WHERE locationId = :locationId and rank >= :fromRank")
    suspend fun moveRanks(locationId: Int, fromRank: Int)
}