package com.aisleron.data.aisle

import com.aisleron.data.AisleronDatabase
import com.aisleron.domain.aisle.Aisle
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

    override suspend fun getDefaultAisles(): List<Aisle> {
        return aisleMapper.toModelList(db.aisleDao().getDefaultAisles())
    }

    override suspend fun getDefaultAisleFor(locationId: Int): Aisle {
        return aisleMapper.toModel(db.aisleDao().getDefaultAisleFor(locationId))
    }

    override suspend fun updateAisleRank(aisle: Aisle) {
        db.aisleDao().updateRank(aisleMapper.fromModel(aisle))
    }

    override suspend fun getWithProducts(aisleId: Int): Aisle {
        return AisleWithProductsMapper().toModel(db.aisleDao().getAisleWithProducts(aisleId))
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
        return db.aisleDao().upsert(aisleMapper.fromModel(item))[0].toInt()
    }

    override suspend fun add(items: List<Aisle>): List<Int> {
        return upsertAisles(items)
    }

    override suspend fun update(item: Aisle) {
        db.aisleDao().upsert(aisleMapper.fromModel(item))
    }

    override suspend fun update(items: List<Aisle>) {
        upsertAisles(items)
    }

    override suspend fun remove(item: Aisle) {
        db.aisleDao().delete(aisleMapper.fromModel(item))
    }

    private suspend fun upsertAisles(aisles: List<Aisle>): List<Int> {
        return db.aisleDao()
            .upsert(
                *aisleMapper.fromModelList(aisles).map { it }.toTypedArray()
            ).map { it.toInt() }
    }
}