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

import com.aisleron.data.base.Mapper
import com.aisleron.domain.productvariant.ProductVariant
import com.aisleron.domain.productvariant.ProductVariantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class ProductVariantRepositoryImpl(
    private val productVariantDao: ProductVariantDao,
    private val productVariantMapper: Mapper<ProductVariantEntity, ProductVariant>
) : ProductVariantRepository {
    private suspend fun getProductVariant(id: Int, includeDeleted: Boolean): ProductVariant? {
        return productVariantDao.getById(id, includeDeleted)
            ?.let { productVariantMapper.toModel(it) }
    }

    override suspend fun get(id: Int): ProductVariant? =
        getProductVariant(id, false)

    override suspend fun getAll(): List<ProductVariant> {
        return productVariantMapper.toModelList(productVariantDao.getAll())
    }

    override suspend fun add(item: ProductVariant): Int {
        return productVariantDao.upsert(productVariantMapper.fromModel(item, null)).single().toInt()
    }

    private suspend fun upsertProductVariants(productVariants: List<ProductVariantEntity>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return productVariantDao
            .upsert(*productVariants.toTypedArray())
            .map { it.toInt() }
    }

    override suspend fun add(items: List<ProductVariant>): List<Int> {
        val productVariants = items.map { productVariantMapper.fromModel(it, null) }
        return upsertProductVariants(productVariants)
    }

    private suspend fun mapExisting(
        item: ProductVariant, includeRemoved: Boolean
    ): ProductVariantEntity {
        val currentEntity = productVariantDao.getById(item.id, includeRemoved)
        return productVariantMapper.fromModel(item, currentEntity)
    }

    override suspend fun update(item: ProductVariant) {
        productVariantDao.upsert(mapExisting(item, false))
    }

    override suspend fun update(items: List<ProductVariant>) {
        val productVariants = items.map { mapExisting(it, false) }
        upsertProductVariants(productVariants)
    }

    override suspend fun remove(item: ProductVariant) {
        val removeEntity = mapExisting(item, false).copy(isRemoved = true)
        productVariantDao.upsert(removeEntity)
    }

    override suspend fun getRemoved(id: Int): ProductVariant? =
        getProductVariant(id, true)

    override suspend fun restore(id: Int) {
        val removedEntity = productVariantDao.getById(id, true) ?: return

        val note = productVariantMapper.toModel(removedEntity)
        val restoreEntity =
            productVariantMapper.fromModel(note, removedEntity).copy(isRemoved = false)

        productVariantDao.upsert(restoreEntity)
    }

    override suspend fun hardDelete(item: ProductVariant) {
        val deletedEntity = mapExisting(item, true)
        productVariantDao.delete(deletedEntity)
    }

    override fun getByBarcode(barcode: String): Flow<ProductVariant?> =
        productVariantDao.getByBarcode(barcode).map { entity ->
            entity?.let { productVariantMapper.toModel(it) }
        }

    override fun getByProductId(productId: Int): Flow<List<ProductVariant>> =
        productVariantDao.getByProductId(productId).map { entities ->
            productVariantMapper.toModelList(entities)
        }

    override fun barcodeExists(barcode: String): Flow<Boolean> =
        productVariantDao.barcodeExists(barcode)

    override fun getProductWithBarcode(barcode: String): Flow<ProductWithBarcode?> =
        productVariantDao.getProductWithBarcode(barcode)

    override suspend fun removeByBarcode(barcode: String) {
        val removeVariant = getByBarcode(barcode).first() ?: return
        val removeEntity = mapExisting(removeVariant, false).copy(isRemoved = true)
        productVariantDao.upsert(removeEntity)
    }
}
