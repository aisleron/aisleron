package com.aisleron.data.aisle

import com.aisleron.data.AisleronDatabase
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleProduct
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.Location

class AisleRepositoryImpl(
    private val db: AisleronDatabase,
    private val aisleMapper: AisleMapper
) : AisleRepository {
    override suspend fun getForLocation(locationId: Int): List<Aisle> {
        return aisleMapper.toModelList(db.aisleDao().getAislesForLocation(locationId))
    }

    override suspend fun getForLocation(location: Location): List<Aisle> {
        return getForLocation(location.id)
    }

    override suspend fun addAisleProducts(aisleProducts: List<AisleProduct>) {
        db.aisleProductDao()
            .upsert(*AisleProductRankMapper().fromModelList(aisleProducts)
                .map { it.aisleProduct }
                .map { it }.toTypedArray()
            )
    }

    override suspend fun getDefaultAisles(): List<Aisle> {
        return aisleMapper.toModelList(db.aisleDao().getDefaultAisles())
    }

    override suspend fun get(id: Int): Aisle? {
        return db.aisleDao().getAisle(id)?.let { aisleMapper.toModel(it) }
    }

    override suspend fun getMultiple(vararg id: Int): List<Aisle> {
        // '*' is a spread operator required to pass vararg down
        return aisleMapper.toModelList(db.aisleDao().getAisles(*id))
    }

    override suspend fun getAll(): List<Aisle> {
        return aisleMapper.toModelList(db.aisleDao().getAisles())
    }

    override suspend fun add(item: Aisle): Int {
        val aisleId = db.aisleDao().upsert(aisleMapper.fromModel(item))[0].toInt()
        if (item.products.isNotEmpty()) {
            addAisleProducts(item.products.map {
                AisleProduct(
                    rank = it.rank,
                    aisleId = aisleId,
                    product = it.product
                )
            })
        }
        return aisleId
    }

    override suspend fun update(item: Aisle) {
        db.aisleDao().upsert(aisleMapper.fromModel(item))
    }

    override suspend fun remove(item: Aisle) {
        db.aisleDao().delete(aisleMapper.fromModel(item))
    }
}