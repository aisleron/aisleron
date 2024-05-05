package com.aisleron.data.product

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.AisleronDatabase
import com.aisleron.data.base.BaseDao

@Dao
abstract class ProductDao(private val db: AisleronDatabase) : BaseDao<ProductEntity> {
    /**
     * Product
     */
    @Transaction
    @Query("SELECT * FROM Product WHERE id = :productId")
    abstract suspend fun getProduct(productId: Int): ProductEntity?

    @Transaction
    @Query("SELECT * FROM Product WHERE id IN (:productId)")
    abstract suspend fun getProducts(vararg productId: Int): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product")
    abstract suspend fun getProducts(): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product p WHERE EXISTS(SELECT NULL FROM AisleProduct ap WHERE aisleId = :aisleId and p.id = ap.productId)")
    abstract suspend fun getProductsForAisle(aisleId: Int): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product WHERE inStock = 1")
    abstract suspend fun getInStockProducts(): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product WHERE inStock = 0")
    abstract suspend fun getNeededProducts(): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM Product WHERE id = :productId")
    suspend fun remove(productId: Int) {
        val product = getProduct(productId)
        product?.let {
            val aisleProducts = db.aisleProductDao().getAisleProductsByProduct(it.id)
            db.aisleProductDao().delete(*aisleProducts.map { it }.toTypedArray())
            delete(product)
        }
    }
}