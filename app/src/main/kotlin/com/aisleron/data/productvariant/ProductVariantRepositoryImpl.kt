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
import kotlinx.coroutines.flow.map

class ProductVariantRepositoryImpl(
    private val productVariantDao: ProductVariantDao,
    private val productVariantMapper: Mapper<ProductVariantEntity, ProductVariant>
) : ProductVariantRepository {

    override suspend fun get(id: Int): ProductVariant? {
        return productVariantDao.getById(id)?.let { productVariantMapper.toModel(it) }
    }

    override suspend fun getAll(): List<ProductVariant> {
        return productVariantMapper.toModelList(productVariantDao.getAll())
    }

    override suspend fun add(item: ProductVariant): Int {
        return productVariantDao.upsert(productVariantMapper.fromModel(item)).single().toInt()
    }

    override suspend fun add(items: List<ProductVariant>): List<Int> {
        return productVariantDao.upsert(
            *productVariantMapper.fromModelList(items).toTypedArray()
        ).map { it.toInt() }
    }

    override suspend fun update(item: ProductVariant) {
        productVariantDao.upsert(productVariantMapper.fromModel(item))
    }

    override suspend fun update(items: List<ProductVariant>) {
        productVariantDao.upsert(
            *productVariantMapper.fromModelList(items).toTypedArray()
        )
    }

    override suspend fun remove(item: ProductVariant) {
        productVariantDao.delete(productVariantMapper.fromModel(item))
    }

    override fun getByBarcode(barcode: String): Flow<ProductVariant?> =
        productVariantDao.getByBarcode(barcode).map { entity ->
            entity?.let { productVariantMapper.toModel(it) }
        }

    override fun getByProductId(productId: Int): Flow<List<ProductVariant>> =
        productVariantDao.getByProductId(productId).map { entities ->
            productVariantMapper.toModelList(entities)
        }

    override fun barcodeExists(barcode: String): Flow<Boolean> = productVariantDao.barcodeExists(barcode)

    override fun getProductWithBarcode(barcode: String): Flow<ProductWithBarcode?> =
        productVariantDao.getProductWithBarcode(barcode)

    override suspend fun deleteByBarcode(barcode: String) {
        productVariantDao.deleteByBarcode(
            ProductVariantDao.BarcodeDeleteHelper(barcode)
        )
    }
}
