package com.aisleron.data.product

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao

@Dao
interface ProductDao : BaseDao<ProductEntity> {
    /**
     * Product
     */
    @Transaction
    @Query("SELECT * FROM Product WHERE id = :productId")
    suspend fun getProduct(productId: Int): ProductEntity

    @Transaction
    @Query("SELECT * FROM Product WHERE id IN (:productId)")
    suspend fun getProducts(vararg productId: Int): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product")
    suspend fun getProducts(): List<ProductEntity>
}