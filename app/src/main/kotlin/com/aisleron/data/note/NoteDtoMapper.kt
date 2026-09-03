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

package com.aisleron.data.note

import com.aisleron.data.sync.DtoMapper
import kotlin.time.Instant

class NoteDtoMapper(private val noteDao: NoteDao) : DtoMapper<NoteEntity, NoteDto> {
    override suspend fun toDto(entity: NoteEntity): NoteDto = NoteDto(
        id = entity.syncId.orEmpty(),
        isDeleted = entity.isRemoved,
        clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString(),
        noteText = entity.noteText,
        createdAt = Instant.fromEpochMilliseconds(entity.createdAt).toString()
    )

    override suspend fun fromDto(dto: NoteDto): NoteEntity {
        val existing = lookupEntityFromDto(dto)

        return NoteEntity(
            id = existing?.id ?: 0,
            noteText = dto.noteText,
            syncId = dto.id,
            isRemoved = dto.isDeleted,
            lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
            serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() },
            createdAt = Instant.parse(dto.createdAt).toEpochMilliseconds()
        )
    }

    override suspend fun lookupEntityFromDto(dto: NoteDto): NoteEntity? {
        noteDao.getBySyncId(dto.id)?.let { return it }

        val createdAt = Instant.parse(dto.createdAt).toEpochMilliseconds()
        val entityList = noteDao.getByNaturalKey(dto.noteText, createdAt)
            .filter { it.syncId == null }

        return entityList.firstOrNull { !it.isRemoved } ?: entityList.firstOrNull()
    }
}
