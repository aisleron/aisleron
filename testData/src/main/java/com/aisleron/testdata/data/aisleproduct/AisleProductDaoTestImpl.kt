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

package com.aisleron.testdata.data.aisleproduct

import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.aisleproduct.AisleProductEntity
import com.aisleron.data.aisleproduct.AisleProductRank
import com.aisleron.testdata.data.product.ProductDaoTestImpl

class AisleProductDaoTestImpl(private val productDao: ProductDaoTestImpl) : AisleProductDao {

    private val aisleProductList = mutableListOf<AisleProductEntity>()

    override suspend fun getAisleProduct(aisleProductId: Int): AisleProductRank? {
        val aisleProduct = aisleProductList.find { it.id == aisleProductId }
        var result: AisleProductRank? = null
        aisleProduct?.let {
            result = AisleProductRank(
                aisleProduct = aisleProduct,
                product = productDao.getProduct(aisleProduct.productId)!!
            )
        }
        return result
    }

    override suspend fun getAisleProductsByProduct(productId: Int): List<AisleProductRank> {
        return aisleProductList.filter { it.productId == productId }.map {
            AisleProductRank(
                aisleProduct = it,
                product = productDao.getProduct(it.productId)!!
            )
        }
    }

    override suspend fun getAisleProducts(): List<AisleProductRank> {
        return aisleProductList.map {
            AisleProductRank(
                aisleProduct = it,
                product = productDao.getProduct(it.productId)!!
            )
        }
    }

    override suspend fun moveRanks(aisleId: Int, fromRank: Int) {
        val aisleProducts = aisleProductList.filter { it.aisleId == aisleId && it.rank >= fromRank }
        aisleProducts.forEach {
            val newAisleProduct = it.copy(rank = it.rank + 1)
            aisleProductList.removeAt(aisleProductList.indexOf(it))
            aisleProductList.add(newAisleProduct)
        }
    }

    override suspend fun removeProductsFromAisle(aisleId: Int) {
        aisleProductList.removeIf { it.aisleId == aisleId }
    }

    override suspend fun getAisleMaxRank(aisleId: Int): Int {
        return aisleProductList.filter { it.aisleId == aisleId }.maxOfOrNull { it.rank } ?: 0
    }

    override suspend fun upsert(vararg entity: AisleProductEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val id: Int
            val existingEntity = aisleProductList.find { ap -> ap.id == it.id }
            if (existingEntity == null) {
                id = (aisleProductList.maxOfOrNull { ap -> ap.id }?.toInt() ?: 0) + 1
            } else {
                id = existingEntity.id
                delete(existingEntity)
            }

            val newEntity = AisleProductEntity(
                id = id,
                rank = it.rank,
                aisleId = it.aisleId,
                productId = it.productId
            )

            aisleProductList.add(newEntity)
            result.add(newEntity.id.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: AisleProductEntity) {
        aisleProductList.removeIf { it in entity }
    }
}