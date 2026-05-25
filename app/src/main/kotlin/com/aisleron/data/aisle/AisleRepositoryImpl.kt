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

package com.aisleron.data.aisle

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository

class AisleRepositoryImpl(
    private val aisleDao: AisleDao,
    private val aisleMapper: AisleMapper
) : AisleRepository {
    override suspend fun getForLocation(locationId: Int): List<Aisle> {
        return aisleMapper.toModelList(aisleDao.getAislesForLocation(locationId))
    }

    override suspend fun getDefaultAisles(): List<Aisle> {
        return aisleMapper.toModelList(aisleDao.getDefaultAisles())
    }

    override suspend fun getDefaultAisleFor(locationId: Int): Aisle? {
        return aisleDao.getDefaultAisleFor(locationId)?.let { aisleMapper.toModel(it) }
    }

    override suspend fun updateAisleRank(aisle: Aisle) {
        aisleDao.updateRank(mapExisting(aisle, false))
    }

    override suspend fun getWithProducts(aisleId: Int): Aisle {
        return AisleWithProductsMapper().toModel(aisleDao.getAisleWithProducts(aisleId))
    }

    override suspend fun getMaxRank(locationId: Int): Int {
        return aisleDao.getMaxRank(locationId)
    }

    private suspend fun getAisle(id: Int, includeDeleted: Boolean): Aisle? {
        return aisleDao.getAisle(id, includeDeleted)?.let { aisleMapper.toModel(it) }
    }

    override suspend fun get(id: Int): Aisle? =
        getAisle(id, false)

    override suspend fun getAll(): List<Aisle> {
        return aisleMapper.toModelList(aisleDao.getAisles())
    }

    override suspend fun add(item: Aisle): Int {
        return aisleDao.upsert(aisleMapper.fromModel(item, null)).single().toInt()
    }

    override suspend fun add(items: List<Aisle>): List<Int> {
        val aisles = items.map { mapExisting(it, false) }
        return upsertAisles(aisles)
    }

    private suspend fun mapExisting(item: Aisle, includeRemoved: Boolean): AisleEntity {
        val currentEntity = aisleDao.getAisle(item.id, includeRemoved)
        return aisleMapper.fromModel(item, currentEntity)
    }

    override suspend fun update(item: Aisle) {
        aisleDao.upsert(mapExisting(item, false))
    }

    override suspend fun update(items: List<Aisle>) {
        val aisles = items.map { mapExisting(it, false) }
        upsertAisles(aisles)
    }

    override suspend fun remove(item: Aisle) {
        val removeEntity = mapExisting(item, false).copy(isRemoved = true)
        aisleDao.upsert(removeEntity)
    }

    override suspend fun getRemoved(id: Int): Aisle? =
        getAisle(id, true)

    override suspend fun restore(id: Int) {
        val removedEntity = aisleDao.getAisle(id, true) ?: return

        val location = aisleMapper.toModel(removedEntity)
        val restoreEntity = aisleMapper.fromModel(
            location, removedEntity
        ).copy(isRemoved = false)

        aisleDao.upsert(restoreEntity)
    }

    override suspend fun hardDelete(item: Aisle) {
        aisleDao.delete(mapExisting(item, true))
    }

    private suspend fun upsertAisles(aisles: List<AisleEntity>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return aisleDao
            .upsert(*aisles.toTypedArray())
            .map { it.toInt() }
    }
}