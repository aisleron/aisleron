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

package com.aisleron.data.aisleproduct

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao

@Dao
interface AisleProductDao : BaseDao<AisleProductEntity> {
    @Transaction
    @Query("SELECT * FROM AisleProduct WHERE id = :aisleProductId AND (isRemoved = 0 OR :includeRemoved = 1)")
    suspend fun getAisleProduct(aisleProductId: Int, includeRemoved: Boolean): AisleProductRank?

    @Transaction
    @Query("SELECT * FROM AisleProduct WHERE productId = :productId AND isRemoved = 0")
    suspend fun getAisleProductsByProduct(productId: Int): List<AisleProductRank>

    @Transaction
    @Query("SELECT * FROM AisleProduct WHERE isRemoved = 0")
    suspend fun getAisleProducts(): List<AisleProductRank>

    @Transaction
    suspend fun updateRank(aisleProduct: AisleProductEntity) {
        moveRanks(aisleProduct.aisleId, aisleProduct.rank)
        upsert(aisleProduct)
    }

    @Query("UPDATE AisleProduct SET rank = rank + 1 WHERE aisleId = :aisleId and rank >= :fromRank")
    suspend fun moveRanks(aisleId: Int, fromRank: Int)

    @Query(
        "UPDATE AisleProduct SET isRemoved = :isRemoved, lastModifiedAt = :lastModifiedAt " +
                "WHERE aisleId = :aisleId"
    )
    suspend fun toggleProductsOnAisleRemove(
        aisleId: Int, isRemoved: Boolean, lastModifiedAt: Long
    )

    @Query("SELECT COALESCE(MAX(rank), 0) FROM AisleProduct WHERE aisleId = :aisleId")
    suspend fun getMaxRank(aisleId: Int): Int
}