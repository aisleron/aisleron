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

    @Transaction
    @Query("SELECT * FROM AisleProduct")
    suspend fun getAisleProducts(): List<AisleProductRank>
}