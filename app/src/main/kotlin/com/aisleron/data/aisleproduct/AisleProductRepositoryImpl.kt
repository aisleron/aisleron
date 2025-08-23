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

package com.aisleron.data.aisleproduct

import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository

class AisleProductRepositoryImpl(
    private val aisleProductDao: AisleProductDao,
    private val aisleProductRankMapper: AisleProductRankMapper
) : AisleProductRepository {
    override suspend fun updateAisleProductRank(item: AisleProduct) {
        aisleProductDao.updateRank(aisleProductRankMapper.fromModel(item).aisleProduct)
    }

    override suspend fun removeProductsFromAisle(aisleId: Int) {
        aisleProductDao.removeProductsFromAisle(aisleId)
    }

    override suspend fun getAisleMaxRank(aisleId: Int): Int {
        return aisleProductDao.getAisleMaxRank(aisleId)
    }

    override suspend fun getProductAisles(productId: Int): List<AisleProduct> {
        return aisleProductRankMapper.toModelList(
            aisleProductDao.getAisleProductsByProduct(productId)
        )
    }

    override suspend fun get(id: Int): AisleProduct? {
        return aisleProductDao.getAisleProduct(id)?.let { aisleProductRankMapper.toModel(it) }
    }

    override suspend fun getAll(): List<AisleProduct> {
        return aisleProductRankMapper.toModelList(aisleProductDao.getAisleProducts())
    }

    override suspend fun add(item: AisleProduct): Int {
        return aisleProductDao
            .upsert(aisleProductRankMapper.fromModel(item).aisleProduct)[0].toInt()
    }

    override suspend fun add(items: List<AisleProduct>): List<Int> {
        return upsertAisleProducts(items)
    }

    private suspend fun upsertAisleProducts(aisleProducts: List<AisleProduct>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return aisleProductDao
            .upsert(*aisleProductRankMapper.fromModelList(aisleProducts)
                .map { it.aisleProduct }
                .map { it }.toTypedArray()
            ).map { it.toInt() }
    }


    override suspend fun update(item: AisleProduct) {
        aisleProductDao.upsert(aisleProductRankMapper.fromModel(item).aisleProduct)
    }

    override suspend fun update(items: List<AisleProduct>) {
        upsertAisleProducts(items)
    }

    override suspend fun remove(item: AisleProduct) {
        aisleProductDao.delete(aisleProductRankMapper.fromModel(item).aisleProduct)
    }
}