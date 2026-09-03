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

package com.aisleron.data.location

import com.aisleron.data.note.NoteDao
import com.aisleron.data.sync.DtoMapper
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import kotlin.time.Instant

class LocationDtoMapper(
    private val locationDao: LocationDao,
    private val noteDao: NoteDao
) : DtoMapper<LocationEntity, LocationDto> {
    override suspend fun toDto(entity: LocationEntity): LocationDto {
        val noteSyncId = entity.noteId?.let { localId -> noteDao.getNote(localId, true)?.syncId }

        return LocationDto(
            id = entity.syncId.orEmpty(),
            isDeleted = entity.isRemoved,
            clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString(),
            type = entity.type.name,
            defaultFilter = entity.defaultFilter.name,
            name = entity.name,
            pinned = entity.pinned,
            noteId = noteSyncId,
            rank = entity.rank
        )
    }

    override suspend fun fromDto(dto: LocationDto): LocationEntity {
        val existing = lookupEntityFromDto(dto)
        val localNoteId = dto.noteId?.let { remoteSyncId -> noteDao.getBySyncId(remoteSyncId)?.id }

        return LocationEntity(
            id = existing?.id ?: 0,
            type = LocationType.valueOf(dto.type),
            defaultFilter = FilterType.valueOf(dto.defaultFilter),
            name = dto.name,
            pinned = dto.pinned,
            showDefaultAisle = existing?.showDefaultAisle ?: true,
            noteId = localNoteId,
            expanded = existing?.expanded ?: true,
            rank = dto.rank,
            syncId = dto.id,
            isRemoved = dto.isDeleted,
            lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
            serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() }
        )
    }

    override suspend fun lookupEntityFromDto(dto: LocationDto): LocationEntity? {
        locationDao.getBySyncId(dto.id)?.let { return it }

        val entityList = locationDao.getByNaturalKey(dto.name, LocationType.valueOf(dto.type))
            .filter { it.syncId == null }

        return entityList.firstOrNull { !it.isRemoved } ?: entityList.firstOrNull()
    }
}