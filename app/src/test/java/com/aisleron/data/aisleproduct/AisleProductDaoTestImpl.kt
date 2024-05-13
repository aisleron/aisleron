package com.aisleron.data.aisleproduct

class AisleProductDaoTestImpl : AisleProductDao {
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
        TODO("Not yet implemented")
    }

    override suspend fun moveRanks(aisleId: Int, fromRank: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun removeProductsFromAisle(aisleId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(vararg entity: AisleProductEntity): List<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(vararg entity: AisleProductEntity) {
        TODO("Not yet implemented")
    }
}