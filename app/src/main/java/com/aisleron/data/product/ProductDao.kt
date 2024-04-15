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
    suspend fun getProduct(productId: Int): ProductEntity?

    @Transaction
    @Query("SELECT * FROM Product WHERE id IN (:productId)")
    suspend fun getProducts(vararg productId: Int): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product")
    suspend fun getProducts(): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product p WHERE EXISTS(SELECT NULL FROM AisleProduct ap WHERE aisleId = :aisleId and p.id = ap.productId)")
    suspend fun getProductsForAisle(aisleId: Int): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product WHERE inStock = 1")
    suspend fun getInStockProducts(): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product WHERE inStock = 0")
    suspend fun getNeededProducts(): List<ProductEntity>

    @Transaction
    @Query("UPDATE Product SET inStock = :inStock WHERE id = :id")
    suspend fun updateStatus(id: Int, inStock: Boolean)
}