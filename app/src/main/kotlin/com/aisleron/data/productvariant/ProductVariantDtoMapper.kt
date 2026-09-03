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

package com.aisleron.data.productvariant

import com.aisleron.data.product.ProductDao
import com.aisleron.data.sync.DtoMapper
import kotlin.time.Instant

class ProductVariantDtoMapper(
    private val productVariantDao: ProductVariantDao,
    private val productDao: ProductDao
) : DtoMapper<ProductVariantEntity, ProductVariantDto> {

    override suspend fun toDto(entity: ProductVariantEntity): ProductVariantDto {
        val productSyncId = checkNotNull(productDao.getProduct(entity.productId, true)?.syncId) {
            "Product syncId not found for product ${entity.productId}"
        }

        return ProductVariantDto(
            id = entity.syncId.orEmpty(),
            isDeleted = entity.isRemoved,
            clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString(),
            productId = productSyncId,
            barcode = entity.barcode,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt).toString()
        )
    }

    private suspend fun getLocalProductId(dto: ProductVariantDto): Int {
        return checkNotNull(productDao.getBySyncId(dto.productId)?.id) {
            "Local product not found for syncId ${dto.productId}"
        }
    }

    override suspend fun fromDto(dto: ProductVariantDto): ProductVariantEntity {
        val existing = lookupEntityFromDto(dto)
        val localProductId = getLocalProductId(dto)

        return ProductVariantEntity(
            id = existing?.id ?: 0,
            productId = localProductId,
            barcode = dto.barcode,
            createdAt = Instant.parse(dto.createdAt).toEpochMilliseconds(),
            syncId = dto.id,
            isRemoved = dto.isDeleted,
            lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
            serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() }
        )
    }

    override suspend fun lookupEntityFromDto(dto: ProductVariantDto): ProductVariantEntity? {
        productVariantDao.getBySyncId(dto.id)?.let { return it }

        val entityList = productVariantDao.getByNaturalKey(dto.barcode)
            .filter { it.syncId == null }

        return entityList.firstOrNull { !it.isRemoved } ?: entityList.firstOrNull()
    }
}