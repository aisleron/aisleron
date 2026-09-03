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
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationDto
import com.aisleron.data.location.LocationDtoMapper
import com.aisleron.data.location.LocationEntity
import com.aisleron.data.note.NoteDao
import com.aisleron.data.note.NoteEntity
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationSyncTest : SyncTest<LocationEntity, LocationDto>() {
    private val locationDao: LocationDao get() = dao as LocationDao

    override fun initSyncApi(): SyncApiTestImpl<LocationDto> =
        SyncApiTestImpl("locations")

    override fun initMapper(): DtoMapper<LocationEntity, LocationDto> =
        LocationDtoMapper(locationDao, get<NoteDao>())

    override fun initDao(): SyncDao<LocationEntity> =
        get<LocationDao>()

    override suspend fun addEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String?
    ): LocationEntity = addLocationEntity(
        lastModifiedAt, serverUpdatedAt, isRemoved, false, syncId
    )

    private suspend fun addNoteEntity(): NoteEntity {
        val entity = NoteEntity(
            id = 0,
            noteText = "Test Note for Location Sync",
            syncId = SyncEntity.generateSyncId()
        )

        val id = get<NoteDao>().upsert(entity).first().toInt()
        return entity.copy(id = id)
    }

    private suspend fun addLocationEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        withNote: Boolean,
        syncId: String? = null
    ): LocationEntity {
        val noteId = if (withNote) addNoteEntity().id else null
        val entity = LocationEntity(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "Location for Sync Test",
            pinned = true,
            showDefaultAisle = true,
            noteId = noteId,
            expanded = true,
            rank = 1,
            isRemoved = isRemoved,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            syncId = syncId
        )

        val id = (dao as LocationDao).upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    override suspend fun addDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): LocationDto = addLocationDto(id, serverUpdatedAt, clientUpdatedAt, isDeleted, false)

    private suspend fun addLocationDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean,
        withNote: Boolean
    ): LocationDto {
        val noteId = if (withNote) addNoteEntity().syncId else null

        val dto = LocationDto(
            id = id,
            isDeleted = isDeleted,
            clientUpdatedAt = clientUpdatedAt,
            serverUpdatedAt = serverUpdatedAt,
            type = LocationType.SHOP.name,
            defaultFilter = FilterType.NEEDED.name,
            name = "Location for Sync Test",
            pinned = true,
            noteId = noteId,
            rank = 1
        )

        syncApi.push(listOf(dto))

        return dto
    }

    override suspend fun validateDtoToEntity(
        dto: LocationDto, compareEntity: LocationEntity
    ): Boolean {
        val expectedEntity = mapper.fromDto(dto).copy(
            id = compareEntity.id,
            showDefaultAisle = compareEntity.showDefaultAisle,
            expanded = compareEntity.expanded
        )

        return expectedEntity == compareEntity
    }

    @Test
    fun toDto_EntityHasNote_DtoHasNote() = runTest {
        val entity = addLocationEntity(100, 100, isRemoved = false, withNote = true)

        val dto = mapper.toDto(entity)

        val noteSyncId = get<NoteDao>().getNote(entity.noteId!!, false)?.syncId
        assertEquals(noteSyncId, dto.noteId)
    }

    @Test
    fun fromDto_DtoHasNote_EntityHasNote() = runTest {
        val dto = addLocationDto(
            SyncEntity.generateSyncId(), "2026-08-18T00:00:00Z", "2026-08-18T05:00:00Z",
            isDeleted = false,
            withNote = true
        )

        val entity = mapper.fromDto(dto)

        val noteId = get<NoteDao>().getBySyncId(dto.noteId!!)?.id
        assertEquals(noteId, entity.noteId)
    }

    @Test
    fun fromDto_ExistingEntityProvided_EntityUpdated() = runTest {
        val syncId = SyncEntity.generateSyncId()
        val existingEntity = addLocationEntity(
            lastModifiedAt = 100L,
            serverUpdatedAt = null,
            isRemoved = false,
            withNote = false,
            syncId = syncId
        )

        val dto = addLocationDto(
            syncId, "2026-08-18T00:00:00Z", "2026-08-18T05:00:00Z",
            isDeleted = false,
            withNote = true
        )

        val mappedEntity = mapper.fromDto(dto)

        assertEquals(existingEntity.id, mappedEntity.id)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnSyncId_ReturnsEntity() = runTest {
        val dto = addDto(
            SyncEntity.generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addLocationEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false,
            withNote = false,
        ).copy(
            syncId = dto.id,
            name = "Not the Same as Dto"
        )

        locationDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertEquals(entity, lookupEntity)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnNaturalKey_ReturnsEntity() = runTest {
        val dto = addDto(
            SyncEntity.generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addLocationEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false,
            withNote = false,
        ).copy(
            name = dto.name
        )

        locationDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertEquals(entity, lookupEntity)
    }

    @Test
    fun lookupEntityFromDto_NoEntityMatch_ReturnsNull() = runTest {
        val dto = addDto(
            SyncEntity.generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addLocationEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false,
            withNote = false,
        ).copy(
            syncId = SyncEntity.generateSyncId(),
            name = "Not the Same as Dto"
        )

        locationDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertNull(lookupEntity)
    }
}