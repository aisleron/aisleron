package com.aisleron.data.aisle

class AisleDaoTestImpl : AisleDao {

    private val aisleList = mutableListOf<AisleEntity>()

    override suspend fun getAisle(aisleId: Int): AisleEntity? {
        return aisleList.find { it.id == aisleId }
    }

    override suspend fun getAisles(vararg aisleId: Int): List<AisleEntity> {
        return aisleList.filter { it.id in aisleId }
    }

    override suspend fun getAisles(): List<AisleEntity> {
        return aisleList
    }

    override suspend fun getAislesForLocation(locationId: Int): List<AisleEntity> {
        return aisleList.filter { it.locationId == locationId }
    }

    override suspend fun getDefaultAisles(): List<AisleEntity> {
        return aisleList.filter { it.isDefault }
    }

    override suspend fun getDefaultAisleFor(locationId: Int): AisleEntity? {
        return aisleList.find { it.locationId == locationId && it.isDefault }
    }

    override suspend fun getAisleWithLocation(aisleId: Int): AisleWithLocation {
        TODO("Not yet implemented")
    }

    override suspend fun getAislesWithLocation(vararg aisleId: Int): List<AisleWithLocation> {
        TODO("Not yet implemented")
    }

    override suspend fun getAislesWithLocation(): List<AisleWithLocation> {
        TODO("Not yet implemented")
    }

    override suspend fun getAisleWithProducts(aisleId: Int): AisleWithProducts {
        return AisleWithProducts(
            aisle = getAisle(aisleId)!!,
            products = emptyList()
        )
    }

    override suspend fun getAislesWithProducts(vararg aisleId: Int): List<AisleWithProducts> {
        TODO("Not yet implemented")
    }

    override suspend fun getAislesWithProducts(): List<AisleWithProducts> {
        TODO("Not yet implemented")
    }

    override suspend fun moveRanks(locationId: Int, fromRank: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(vararg entity: AisleEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val id: Int
            val existingEntity = getAisle(it.id)
            if (existingEntity == null) {
                id = (aisleList.maxOfOrNull { a -> a.id }?.toInt() ?: 0) + 1
            } else {
                id = existingEntity.id
                delete(existingEntity)
            }

            val newEntity = AisleEntity(
                id = id,
                name = it.name,
                rank = it.rank,
                locationId = it.locationId,
                isDefault = it.isDefault
            )

            aisleList.add(newEntity)
            result.add(newEntity.id.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: AisleEntity) {
        aisleList.removeIf { it in entity }
    }
}