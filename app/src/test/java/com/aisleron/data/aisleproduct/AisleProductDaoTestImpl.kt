package com.aisleron.data.aisleproduct

import com.aisleron.data.product.ProductEntity

class AisleProductDaoTestImpl : AisleProductDao {

    private val aisleProductList = mutableListOf<AisleProductEntity>()

    override suspend fun getAisleProduct(aisleProductId: Int): AisleProductRank? {
        TODO("Not yet implemented")
    }

    override suspend fun getAisleProducts(vararg aisleProductId: Int): List<AisleProductRank> {
        TODO("Not yet implemented")
    }

    override suspend fun getAisleProductsByProduct(productId: Int): List<AisleProductEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getAisleProducts(): List<AisleProductRank> {
        return aisleProductList.map {
            AisleProductRank(
                aisleProduct = it,
                product = ProductEntity(1, "Dummy Product", false)
            )
        }
    }

    override suspend fun moveRanks(aisleId: Int, fromRank: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun removeProductsFromAisle(aisleId: Int) {
        aisleProductList.removeIf { it.aisleId == aisleId }
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