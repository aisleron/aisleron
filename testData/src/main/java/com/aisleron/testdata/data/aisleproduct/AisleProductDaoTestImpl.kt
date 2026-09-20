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

package com.aisleron.testdata.data.aisleproduct

import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.aisleproduct.AisleProductEntity
import com.aisleron.data.aisleproduct.AisleProductRank
import com.aisleron.testdata.data.aisle.AisleDaoTestImpl
import com.aisleron.testdata.data.base.BaseSyncTestDao
import com.aisleron.testdata.data.product.ProductDaoTestImpl

class AisleProductDaoTestImpl(
    private val productDao: ProductDaoTestImpl,
) : BaseSyncTestDao<AisleProductEntity>(), AisleProductDao {

    override suspend fun getAisleProduct(
        aisleProductId: Int, includeRemoved: Boolean
    ): AisleProductRank? {
        val aisleProduct =
            entityList.find { it.id == aisleProductId && (!it.isRemoved || includeRemoved) }

        var result: AisleProductRank? = null
        aisleProduct?.let {
            result = AisleProductRank(
                aisleProduct = aisleProduct,
                product = productDao.getProduct(aisleProduct.productId, includeRemoved)!!
            )
        }
        return result
    }

    override suspend fun getAisleProductsByProduct(productId: Int): List<AisleProductRank> {
        return activeItems.filter { it.productId == productId }.map {
            AisleProductRank(
                aisleProduct = it,
                product = productDao.getProduct(it.productId, false)!!
            )
        }
    }

    override suspend fun getAisleProducts(): List<AisleProductRank> {
        return activeItems.map {
            AisleProductRank(
                aisleProduct = it,
                product = productDao.getProduct(it.productId, false)!!
            )
        }
    }

    override suspend fun moveRanks(aisleId: Int, fromRank: Int) {
        val aisleProducts = entityList.filter { it.aisleId == aisleId && it.rank >= fromRank }
        aisleProducts.forEach {
            val newAisleProduct = it.copy(rank = it.rank + 1)
            entityList.removeAt(entityList.indexOf(it))
            entityList.add(newAisleProduct)
        }
    }

    override suspend fun toggleProductsOnAisleRemove(
        aisleId: Int, isRemoved: Boolean, lastModifiedAt: Long
    ) {
        val entities = getAisleProducts().filter { it.aisleProduct.aisleId == aisleId }.map {
            it.aisleProduct.copy(
                isRemoved = isRemoved,
                lastModifiedAt = lastModifiedAt
            )
        }

        upsert(*entities.toTypedArray())
    }

    override suspend fun getMaxRank(aisleId: Int): Int {
        return entityList.filter { it.aisleId == aisleId }.maxOfOrNull { it.rank } ?: 0
    }

    override suspend fun getByLocationNaturalKey(
        locationId: Int, productId: Int
    ): List<AisleProductEntity> {
        val aisleDao = AisleDaoTestImpl(this)
        val aisleIds = aisleDao.getAislesForLocation(locationId).mapTo(HashSet()) { it.id }
        return entityList.filter { it.productId == productId && it.aisleId in aisleIds }
    }

    override suspend fun upsert(vararg entity: AisleProductEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val id: Int
            val existingEntity = entityList.find { ap -> ap.id == it.id }
            if (existingEntity == null) {
                id = (entityList.maxOfOrNull { ap -> ap.id } ?: 0) + 1
            } else {
                id = existingEntity.id
                delete(existingEntity)
            }

            val newEntity = AisleProductEntity(
                id = id,
                rank = it.rank,
                aisleId = it.aisleId,
                productId = it.productId,
                lastModifiedAt = it.lastModifiedAt,
                isRemoved = it.isRemoved,
                syncId = it.syncId,
                serverUpdatedAt = it.serverUpdatedAt
            )

            entityList.add(newEntity)
            result.add(newEntity.id.toLong())
        }

        return result
    }

    override suspend fun upsert(entities: List<AisleProductEntity>) {
        upsert(*entities.toTypedArray())
    }
}