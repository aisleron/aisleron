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
    @Query("SELECT * FROM AisleProduct WHERE id in (:aisleProductId)")
    suspend fun getAisleProducts(vararg aisleProductId: Int): List<AisleProductRank>

    @Query("SELECT * FROM AisleProduct WHERE productId = :productId")
    suspend fun getAisleProductsByProduct(productId: Int): List<AisleProductEntity>

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
}