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

import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val aisleProductDao: AisleProductDao,
    private val productMapper: ProductMapper
) : ProductRepository {
    override suspend fun getByName(name: String): Product? {
        return productDao.getProductByName(name.trim())?.let { return productMapper.toModel(it) }
    }

    override suspend fun get(id: Int): Product? {
        return productDao.getProduct(id)?.let { productMapper.toModel(it) }
    }

    override suspend fun getAll(): List<Product> {
        return productMapper.toModelList(productDao.getProducts())
    }

    override suspend fun add(item: Product): Int {
        return productDao.upsert(productMapper.fromModel(item))[0].toInt()
    }

    override suspend fun add(items: List<Product>): List<Int> {
        return upsertProducts(items)
    }

    override suspend fun update(item: Product) {
        productDao.upsert(productMapper.fromModel(item))
    }

    override suspend fun update(items: List<Product>) {
        upsertProducts(items)
    }

    private suspend fun upsertProducts(products: List<Product>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return productDao
            .upsert(*productMapper.fromModelList(products).map { it }.toTypedArray())
            .map { it.toInt() }
    }

    override suspend fun remove(item: Product) {
        productDao.remove(productMapper.fromModel(item), aisleProductDao)
    }
}