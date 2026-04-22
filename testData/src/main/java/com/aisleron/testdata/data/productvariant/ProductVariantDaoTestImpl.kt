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

package com.aisleron.testdata.data.productvariant

import com.aisleron.data.product.ProductDao
import com.aisleron.data.productvariant.ProductVariantDao
import com.aisleron.data.productvariant.ProductVariantEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ProductVariantDaoTestImpl(
    private val productDao: ProductDao
) : ProductVariantDao {
    private val variants = mutableListOf<ProductVariantEntity>()

    override suspend fun getAll(): List<ProductVariantEntity> = variants.toList()

    override suspend fun getById(id: Int): ProductVariantEntity? = variants.find { it.id == id }

    override fun getByBarcode(barcode: String): Flow<ProductVariantEntity?> =
        flowOf(variants.find { it.barcode == barcode })

    override fun getByProductId(productId: Int): Flow<List<ProductVariantEntity>> =
        flowOf(variants.filter { it.productId == productId })

    override fun barcodeExists(barcode: String): Flow<Boolean> =
        flowOf(variants.any { it.barcode == barcode })

    override suspend fun deleteByBarcode(barcode: ProductVariantDao.BarcodeDeleteHelper): Int {
        val initialSize = variants.size
        variants.removeAll { it.barcode == barcode.value }
        return initialSize - variants.size
    }

    override fun getProductWithBarcode(barcode: String): Flow<com.aisleron.data.productvariant.ProductWithBarcode?> = flowOf(
        kotlinx.coroutines.runBlocking {
            val variant = variants.find { it.barcode == barcode } ?: return@runBlocking null
            val product = productDao.getProduct(variant.productId) ?: return@runBlocking null
            com.aisleron.data.productvariant.ProductWithBarcode(
                variant = variant,
                product = product
            )
        }
    )

    override suspend fun upsert(vararg entity: ProductVariantEntity): List<Long> {
        val ids = mutableListOf<Long>()
        entity.forEach { newEntity ->
            val existingIndex = variants.indexOfFirst { it.id == newEntity.id }
            if (existingIndex >= 0) {
                variants[existingIndex] = newEntity
                ids.add(newEntity.id.toLong())
            } else {
                val newId = (variants.maxOfOrNull { it.id } ?: 0) + 1
                val entityWithId = newEntity.copy(id = newId)
                variants.add(entityWithId)
                ids.add(newId.toLong())
            }
        }
        return ids
    }

    override suspend fun delete(vararg entity: ProductVariantEntity) {
        entity.forEach { toRemove ->
            variants.removeAll { it.id == toRemove.id }
        }
    }

    override suspend fun getProductIdsWithVariants(productIds: List<Int>): List<Int> =
        variants.map { it.productId }.distinct().filter { it in productIds }

    override suspend fun hasVariants(productId: Int): Boolean =
        variants.any { it.productId == productId }

    fun clear() {
        variants.clear()
    }
}
