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

    override suspend fun get(id: Int): AisleProduct? {
        return aisleProductDao.getAisleProduct(id)?.let { aisleProductRankMapper.toModel(it) }
    }

    override suspend fun getMultiple(vararg id: Int): List<AisleProduct> {
        // '*' is a spread operator required to pass vararg down
        return aisleProductRankMapper.toModelList(aisleProductDao.getAisleProducts(*id))
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