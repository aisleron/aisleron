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

package com.aisleron.data.product

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.base.BaseDao

@Dao
interface ProductDao : BaseDao<ProductEntity> {
    /**
     * Product
     */
    @Query("SELECT * FROM Product WHERE id = :productId")
    suspend fun getProduct(productId: Int): ProductEntity?

    @Query("SELECT * FROM Product")
    suspend fun getProducts(): List<ProductEntity>

    @Transaction
    suspend fun remove(product: ProductEntity, aisleProductDao: AisleProductDao) {
        val aisleProducts = aisleProductDao.getAisleProductsByProduct(product.id)
        aisleProductDao.delete(*aisleProducts.map { it.aisleProduct }.toTypedArray())
        delete(product)
    }

    @Query("SELECT * FROM Product WHERE name = :name COLLATE NOCASE")
    suspend fun getProductByName(name: String): ProductEntity?
}