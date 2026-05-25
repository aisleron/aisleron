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

package com.aisleron.data.productvariant

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductVariantDao : BaseDao<ProductVariantEntity> {
    @Query("SELECT * FROM ProductVariant WHERE isRemoved = 0 ORDER BY createdAt ASC")
    suspend fun getAll(): List<ProductVariantEntity>

    @Query("SELECT * FROM ProductVariant WHERE id = :id AND (isRemoved = 0 OR :includeRemoved = 1)")
    suspend fun getById(id: Int, includeRemoved: Boolean): ProductVariantEntity?

    @Query("SELECT * FROM ProductVariant WHERE barcode = :barcode AND isRemoved = 0 LIMIT 1")
    fun getByBarcode(barcode: String): Flow<ProductVariantEntity?>

    @Query("SELECT * FROM ProductVariant WHERE productId = :productId AND isRemoved = 0 ORDER BY createdAt ASC")
    fun getByProductId(productId: Int): Flow<List<ProductVariantEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM ProductVariant WHERE barcode = :barcode AND isRemoved = 0 LIMIT 1)")
    fun barcodeExists(barcode: String): Flow<Boolean>

    @Transaction
    @Query("SELECT * FROM ProductVariant WHERE barcode = :barcode AND isRemoved = 0 LIMIT 1")
    fun getProductWithBarcode(barcode: String): Flow<ProductWithBarcode?>

    @Query("SELECT DISTINCT productId FROM ProductVariant WHERE productId IN (:productIds) AND isRemoved = 0")
    suspend fun getProductIdsWithVariants(productIds: List<Int>): List<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM ProductVariant WHERE productId = :productId AND isRemoved = 0 LIMIT 1)")
    suspend fun hasVariants(productId: Int): Boolean
}
