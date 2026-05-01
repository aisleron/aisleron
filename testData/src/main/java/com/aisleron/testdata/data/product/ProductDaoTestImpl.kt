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

package com.aisleron.testdata.data.product

import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductEntity
import com.aisleron.testdata.data.aisleproduct.AisleProductDaoTestImpl

class ProductDaoTestImpl : ProductDao {
    private val productList = mutableListOf<ProductEntity>()
    private val activeItems: List<ProductEntity> get() = productList.filter { !it.isRemoved }


    override suspend fun upsert(vararg entity: ProductEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val id: Int
            val existingEntity = getProduct(it.id, true)
            if (existingEntity == null) {
                id = (productList.maxOfOrNull { e -> e.id } ?: 0) + 1
            } else {
                id = existingEntity.id
                productList.removeAt(productList.indexOf(existingEntity))
            }

            val newEntity = ProductEntity(
                id = id,
                name = it.name,
                inStock = it.inStock,
                qtyNeeded = it.qtyNeeded,
                noteId = it.noteId,
                qtyIncrement = it.qtyIncrement,
                trackingMode = it.trackingMode,
                unitOfMeasure = it.unitOfMeasure
            )

            productList.add(newEntity)
            result.add(newEntity.id.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: ProductEntity) {
        productList.removeIf { it in entity }
    }

    override suspend fun getProduct(productId: Int, includeRemoved: Boolean): ProductEntity? {
        return productList.find { it.id == productId && (!it.isRemoved || includeRemoved) }
    }

    override suspend fun getProducts(): List<ProductEntity> = activeItems

    override suspend fun getProductByName(name: String): ProductEntity? {
        return activeItems.find { it.name.equals(name, ignoreCase = true) }
    }

    override suspend fun toggleAisleProductRemove(
        productId: Int, isRemoved: Boolean, lastModifiedAt: Long
    ) {
        val aisleProductDao = AisleProductDaoTestImpl(this)
        val entities = aisleProductDao.getAisleProductsByProduct(productId).map {
            it.aisleProduct.copy(isRemoved = isRemoved, lastModifiedAt = lastModifiedAt)
        }

        aisleProductDao.upsert(*entities.toTypedArray())
    }
}