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

package com.aisleron.data.loyaltycard

import com.aisleron.data.sync.DtoMapper
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import kotlin.time.Instant

class LoyaltyCardDtoMapper(
    private val loyaltyCardDao: LoyaltyCardDao
) : DtoMapper<LoyaltyCardEntity, LoyaltyCardDto> {

    override suspend fun toDto(entity: LoyaltyCardEntity): LoyaltyCardDto = LoyaltyCardDto(
        id = entity.syncId.orEmpty(),
        isDeleted = entity.isRemoved,
        clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString(),
        name = entity.name,
        provider = entity.provider.name,
        intent = entity.intent
    )

    override suspend fun fromDto(dto: LoyaltyCardDto): LoyaltyCardEntity {
        val existing = lookupEntityFromDto(dto)

        return LoyaltyCardEntity(
            id = existing?.id ?: 0,
            name = dto.name,
            provider = LoyaltyCardProviderType.valueOf(dto.provider),
            intent = dto.intent,
            syncId = dto.id,
            isRemoved = dto.isDeleted,
            lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
            serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() }
        )
    }

    override suspend fun lookupEntityFromDto(dto: LoyaltyCardDto): LoyaltyCardEntity? {
        loyaltyCardDao.getBySyncId(dto.id)?.let { return it }

        val entityList = loyaltyCardDao.getByNaturalKey(
            LoyaltyCardProviderType.valueOf(dto.provider), dto.intent
        ).filter { it.syncId == null }

        return entityList.firstOrNull { !it.isRemoved } ?: entityList.firstOrNull()
    }
}