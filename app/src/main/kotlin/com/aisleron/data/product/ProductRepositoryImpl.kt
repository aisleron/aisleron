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

package com.aisleron.data.product

import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val aisleProductDao: AisleProductDao,
    private val productMapper: ProductMapper
) : ProductRepository {
    override suspend fun getByName(name: String): Product? {
        return productDao.getProductByName(name.trim())?.let { productMapper.toModel(it) }
    }

    override suspend fun get(id: Int): Product? =
        getProduct(id, false)

    override suspend fun getAll(): List<Product> {
        return productMapper.toModelList(productDao.getProducts())
    }

    override suspend fun add(item: Product): Int {
        return productDao.upsert(productMapper.fromModel(item, null)).single().toInt()
    }

    override suspend fun add(items: List<Product>): List<Int> {
        val products = items.map { productMapper.fromModel(it, null) }
        return upsertProducts(products)
    }

    private suspend fun mapExisting(item: Product, includeDeleted: Boolean): ProductEntity {
        val currentEntity = productDao.getProduct(item.id, includeDeleted)
        return productMapper.fromModel(item, currentEntity)
    }

    override suspend fun update(item: Product) {
        productDao.upsert(mapExisting(item, false))
    }

    override suspend fun update(items: List<Product>) {
        val products = items.map { mapExisting(it, false) }
        upsertProducts(products)
    }

    private suspend fun upsertProducts(products: List<ProductEntity>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return productDao
            .upsert(*products.toTypedArray())
            .map { it.toInt() }
    }

    override suspend fun remove(item: Product) {
        val removeEntity = mapExisting(item, false).copy(isRemoved = true)
        productDao.updateProductRemovedState(removeEntity)
    }

    override suspend fun getRemoved(id: Int): Product? =
        getProduct(id, true)

    override suspend fun restore(id: Int) {
        val removedEntity = productDao.getProduct(id, true) ?: return

        val product = productMapper.toModel(removedEntity)
        val restoreEntity = productMapper.fromModel(product, removedEntity).copy(isRemoved = false)
        productDao.updateProductRemovedState(restoreEntity)
    }

    override suspend fun hardDelete(item: Product) {
        productDao.delete(mapExisting(item, true), aisleProductDao)
    }

    private suspend fun getProduct(id: Int, includeDeleted: Boolean): Product? {
        return productDao.getProduct(id, includeDeleted)?.let { productMapper.toModel(it) }
    }
}