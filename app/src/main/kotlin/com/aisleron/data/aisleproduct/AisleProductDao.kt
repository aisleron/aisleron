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

package com.aisleron.data.aisleproduct

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao

@Dao
interface AisleProductDao : BaseDao<AisleProductEntity> {

    @Transaction
    @Query("SELECT * FROM AisleProduct WHERE id = :aisleProductId")
    suspend fun getAisleProduct(aisleProductId: Int): AisleProductRank?

    @Transaction
    @Query("SELECT * FROM AisleProduct WHERE productId = :productId")
    suspend fun getAisleProductsByProduct(productId: Int): List<AisleProductRank>

    @Transaction
    @Query("SELECT * FROM AisleProduct")
    suspend fun getAisleProducts(): List<AisleProductRank>

    @Transaction
    suspend fun updateRank(aisleProduct: AisleProductEntity) {
        moveRanks(aisleProduct.aisleId, aisleProduct.rank)
        upsert(aisleProduct)
    }

    @Query("UPDATE AisleProduct SET rank = rank + 1 WHERE aisleId = :aisleId and rank >= :fromRank")
    suspend fun moveRanks(aisleId: Int, fromRank: Int)

    @Query("DELETE FROM AisleProduct WHERE aisleId = :aisleId")
    suspend fun removeProductsFromAisle(aisleId: Int)

    @Query("SELECT COALESCE(MAX(rank), 0) FROM AisleProduct WHERE aisleId = :aisleId")
    suspend fun getAisleMaxRank(aisleId: Int): Int
}