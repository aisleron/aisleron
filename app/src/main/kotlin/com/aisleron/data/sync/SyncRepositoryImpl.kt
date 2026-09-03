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

package com.aisleron.data.sync

import com.aisleron.data.base.SyncEntity
import kotlin.collections.maxByOrNull

class SyncRepositoryImpl<Entity : SyncEntity, Dto : SyncDto>(
    override val syncOrder: Int,
    private val dao: SyncDao<Entity>,
    private val syncApi: SyncApi<Dto>,
    private val dtoMapper: DtoMapper<Entity, Dto>
) : SyncRepository {
    override val remoteEntityName: String
        get() = syncApi.entityName

    override suspend fun push(modifiedAfterDate: Long) {
        val modifiedLocal = dao.getModified(modifiedAfterDate)
            .filterNot { it.syncId == null && it.isRemoved }

        if (modifiedLocal.isEmpty()) return

        val dto = modifiedLocal.map { dtoMapper.toDto(it) }

        syncApi.push(dto)
    }

    override suspend fun pull(serverLastUpdatedDateIso: String): String {
        val remoteDto = syncApi.fetchSince(serverLastUpdatedDateIso)
        if (remoteDto.isEmpty()) return serverLastUpdatedDateIso

        val entitiesToSave = remoteDto.map { dto ->
            dtoMapper.fromDto(dto)
        }

        dao.upsert(entitiesToSave)

        return remoteDto.maxByOrNull { it.serverUpdatedAt ?: "" }
            ?.serverUpdatedAt
            ?: serverLastUpdatedDateIso
    }

    override suspend fun purgeRemoved(purgeToDate: Long) {
        dao.purgeRemoved(purgeToDate)
    }
}