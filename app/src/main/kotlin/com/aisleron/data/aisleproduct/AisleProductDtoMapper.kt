/*
 * Copyright (C) 2026 aisleron.com
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

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductEntity
import com.aisleron.data.sync.DtoMapper
import kotlin.time.Instant

class AisleProductDtoMapper(
    private val aisleProductDao: AisleProductDao,
    private val aisleDao: AisleDao,
    private val productDao: ProductDao
) : DtoMapper<AisleProductEntity, AisleProductDto> {

    override suspend fun toDto(entity: AisleProductEntity): AisleProductDto {
        val aisleSyncId = checkNotNull(aisleDao.getAisle(entity.aisleId, true)?.syncId) {
            "Aisle syncId not found for aisle ${entity.aisleId}"
        }

        val productSyncId = checkNotNull(productDao.getProduct(entity.productId, true)?.syncId) {
            "Product syncId not found for product ${entity.productId}"
        }

        return AisleProductDto(
            id = entity.syncId,
            isDeleted = entity.isRemoved,
            clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString(),
            aisleId = aisleSyncId,
            productId = productSyncId,
            rank = entity.rank
        )
    }

    private suspend fun getLocalAisle(dto: AisleProductDto): AisleEntity {
        return checkNotNull(aisleDao.getBySyncId(dto.aisleId)) {
            "Local aisle not found for syncId ${dto.aisleId}"
        }
    }

    private suspend fun getLocalProduct(dto: AisleProductDto): ProductEntity {
        return checkNotNull(productDao.getBySyncId(dto.productId)) {
            "Local product not found for syncId ${dto.productId}"
        }
    }

    override suspend fun fromDto(dto: AisleProductDto): AisleProductEntity {
        val existing = lookupEntityFromDto(dto)
        val localAisleId = getLocalAisle(dto).id
        val localProductId = getLocalProduct(dto).id

        return AisleProductEntity(
            id = existing?.id ?: 0,
            aisleId = localAisleId,
            productId = localProductId,
            rank = dto.rank,
            syncId = dto.id,
            isRemoved = dto.isDeleted,
            lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
            serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() }
        )
    }

    override suspend fun lookupEntityFromDto(dto: AisleProductDto): AisleProductEntity? {
        aisleProductDao.getBySyncId(dto.id)?.let { return it }

        val localLocationId = getLocalAisle(dto).locationId
        val localProductId = getLocalProduct(dto).id
        val entityList = aisleProductDao.getByNaturalKey(localLocationId, localProductId)

        return entityList.firstOrNull { !it.isRemoved } ?: entityList.firstOrNull()
    }
}