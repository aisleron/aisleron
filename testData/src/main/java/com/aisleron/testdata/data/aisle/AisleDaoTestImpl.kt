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

package com.aisleron.testdata.data.aisle

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.aisle.AisleWithProducts
import com.aisleron.testdata.data.aisleproduct.AisleProductDaoTestImpl

class AisleDaoTestImpl(private val aisleProductDao: AisleProductDaoTestImpl) : AisleDao {

    private val aisleList = mutableListOf<AisleEntity>()

    override suspend fun getAisle(aisleId: Int): AisleEntity? {
        return aisleList.find { it.id == aisleId }
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

    override suspend fun getAisleWithProducts(aisleId: Int): AisleWithProducts {
        return AisleWithProducts(
            aisle = getAisle(aisleId)!!,
            products = aisleProductDao.getAisleProducts()
                .filter { ap -> ap.aisleProduct.aisleId == aisleId }
        )
    }

    override suspend fun getAislesWithProducts(): List<AisleWithProducts> {
        return aisleList.map {
            AisleWithProducts(
                aisle = it,
                products = aisleProductDao.getAisleProducts()
                    .filter { ap -> ap.aisleProduct.aisleId == it.id }
            )
        }
    }

    override suspend fun moveRanks(locationId: Int, fromRank: Int) {
        val locationAisles = aisleList.filter { it.locationId == locationId && it.rank >= fromRank }
        locationAisles.forEach {
            val newAisle = it.copy(rank = it.rank + 1)
            aisleList.removeAt(aisleList.indexOf(it))
            aisleList.add(newAisle)
        }
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
                isDefault = it.isDefault,
                expanded = it.expanded
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