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
    fun getProduct(productId: Int): ProductEntity

    @Transaction
    @Query("SELECT * FROM Product WHERE id IN (:productId)")
    fun getProducts(vararg productId: Int): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product")
    fun getProducts(): List<ProductEntity>
}