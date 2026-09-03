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

package com.aisleron.data.aisle

import com.aisleron.data.location.LocationDao
import com.aisleron.data.sync.DtoMapper
import kotlin.time.Instant

class AisleDtoMapper(
    private val aisleDao: AisleDao,
    private val locationDao: LocationDao
) : DtoMapper<AisleEntity, AisleDto> {

    override suspend fun toDto(entity: AisleEntity): AisleDto {
        val locationSyncId =
            checkNotNull(locationDao.getLocation(entity.locationId, true)?.syncId) {
                "Location syncId missing for local locationId ${entity.locationId}"
            }

        return AisleDto(
            id = entity.syncId.orEmpty(),
            isDeleted = entity.isRemoved,
            clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString(),
            name = entity.name,
            locationId = locationSyncId,
            rank = entity.rank,
            isDefault = entity.isDefault
        )
    }

    private suspend fun getLocalLocationId(dto: AisleDto): Int {
        return checkNotNull(locationDao.getBySyncId(dto.locationId)?.id) {
            "Local location not found for syncId ${dto.locationId}"
        }
    }

    override suspend fun fromDto(dto: AisleDto): AisleEntity {
        val existing = lookupEntityFromDto(dto)
        val localLocationId = getLocalLocationId(dto)

        return AisleEntity(
            id = existing?.id ?: 0,
            name = dto.name,
            locationId = localLocationId,
            rank = dto.rank,
            isDefault = dto.isDefault,
            expanded = existing?.expanded ?: true,
            syncId = dto.id,
            isRemoved = dto.isDeleted,
            lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
            serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() }
        )
    }

    override suspend fun lookupEntityFromDto(dto: AisleDto): AisleEntity? {
        aisleDao.getBySyncId(dto.id)?.let { return it }

        val localLocationId = getLocalLocationId(dto)
        val entityList = aisleDao.getByNaturalKey(dto.name, localLocationId)
            .filter { it.syncId == null }

        return entityList.firstOrNull { !it.isRemoved } ?: entityList.firstOrNull()
    }
}