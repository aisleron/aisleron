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

package com.aisleron.data.aisleproduct

import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository

class AisleProductRepositoryImpl(
    private val aisleProductDao: AisleProductDao,
    private val aisleProductRankMapper: AisleProductRankMapper
) : AisleProductRepository {
    private suspend fun mapExisting(
        item: AisleProduct, includeDeleted: Boolean
    ): AisleProductRank {
        val currentEntity = aisleProductDao.getAisleProduct(item.id, includeDeleted)
        return aisleProductRankMapper.fromModel(item, currentEntity?.aisleProduct)
    }

    override suspend fun updateAisleProductRank(item: AisleProduct) {
        val existingEntity = mapExisting(item, false).aisleProduct
        aisleProductDao.updateRank(existingEntity)
    }

    override suspend fun removeProductsFromAisle(aisleId: Int) {
        aisleProductDao.toggleProductsOnAisleRemove(aisleId, true, System.currentTimeMillis())
    }

    override suspend fun restoreProductsToAisle(aisleId: Int) {
        aisleProductDao.toggleProductsOnAisleRemove(aisleId, false, System.currentTimeMillis())
    }

    override suspend fun getMaxRank(aisleId: Int): Int {
        return aisleProductDao.getMaxRank(aisleId)
    }

    override suspend fun getProductAisles(productId: Int): List<AisleProduct> {
        return aisleProductRankMapper.toModelList(
            aisleProductDao.getAisleProductsByProduct(productId)
        )
    }

    override suspend fun get(id: Int): AisleProduct? =
        getAisleProduct(id, false)

    override suspend fun getAll(): List<AisleProduct> {
        return aisleProductRankMapper.toModelList(aisleProductDao.getAisleProducts())
    }

    override suspend fun add(item: AisleProduct): Int {
        return aisleProductDao
            .upsert(aisleProductRankMapper.fromModel(item, null).aisleProduct)[0].toInt()
    }

    override suspend fun add(items: List<AisleProduct>): List<Int> {
        val aisleProducts = items.map { aisleProductRankMapper.fromModel(it, null) }
        return upsertAisleProducts(aisleProducts)
    }

    private suspend fun upsertAisleProducts(aisleProducts: List<AisleProductRank>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return aisleProductDao
            .upsert(
                *aisleProducts
                    .map { it.aisleProduct }
                    .map { it }.toTypedArray()
            ).map { it.toInt() }
    }


    override suspend fun update(item: AisleProduct) {
        aisleProductDao.upsert(mapExisting(item, false).aisleProduct)
    }

    override suspend fun update(items: List<AisleProduct>) {
        val aisleProducts = items.map { mapExisting(it, false) }
        upsertAisleProducts(aisleProducts)
    }

    override suspend fun remove(item: AisleProduct) {
        val removeEntity = mapExisting(item, false).aisleProduct.copy(isRemoved = true)
        aisleProductDao.upsert(removeEntity)
    }

    override suspend fun getRemoved(id: Int): AisleProduct? =
        getAisleProduct(id, true)

    override suspend fun restore(id: Int) {
        val removedEntity = aisleProductDao.getAisleProduct(id, true) ?: return

        val aisleProduct = aisleProductRankMapper.toModel(removedEntity)
        val restoreEntity = aisleProductRankMapper.fromModel(
            aisleProduct, removedEntity.aisleProduct
        ).aisleProduct.copy(isRemoved = false)
        aisleProductDao.upsert(restoreEntity)
    }

    override suspend fun hardDelete(item: AisleProduct) {
        val deleteEntity = mapExisting(item, true).aisleProduct
        aisleProductDao.delete(deleteEntity)
    }

    private suspend fun getAisleProduct(id: Int, includeDeleted: Boolean): AisleProduct? {
        return aisleProductDao.getAisleProduct(id, includeDeleted)
            ?.let { aisleProductRankMapper.toModel(it) }
    }
}