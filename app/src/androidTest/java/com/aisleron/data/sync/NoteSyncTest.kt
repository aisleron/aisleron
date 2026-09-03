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
import com.aisleron.data.note.NoteDao
import com.aisleron.data.note.NoteDto
import com.aisleron.data.note.NoteDtoMapper
import com.aisleron.data.note.NoteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NoteSyncTest : SyncTest<NoteEntity, NoteDto>() {
    private val noteDao: NoteDao get() = dao as NoteDao

    override fun initSyncApi(): SyncApiTestImpl<NoteDto> =
        SyncApiTestImpl("notes")

    override fun initMapper(): DtoMapper<NoteEntity, NoteDto> =
        NoteDtoMapper(noteDao)

    override fun initDao(): SyncDao<NoteEntity> =
        get<NoteDao>()

    override suspend fun addEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String?
    ): NoteEntity = addNoteEntity(lastModifiedAt, serverUpdatedAt, isRemoved, syncId)

    override suspend fun addDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): NoteDto {
        val dto = NoteDto(
            id = id,
            isDeleted = isDeleted,
            clientUpdatedAt = clientUpdatedAt,
            serverUpdatedAt = serverUpdatedAt,
            noteText = "Note for Sync Test",
            createdAt = Clock.System.now().toString()
        )

        syncApi.push(listOf(dto))

        return dto
    }

    override suspend fun validateDtoToEntity(
        dto: NoteDto, compareEntity: NoteEntity
    ): Boolean {
        val expectedEntity = mapper.fromDto(dto).copy(
            id = compareEntity.id
        )

        return expectedEntity == compareEntity
    }

    private suspend fun addNoteEntity(
        lastModifiedAt: Long = 0,
        serverUpdatedAt: Long? = null,
        isRemoved: Boolean = false,
        syncId: String? = null
    ): NoteEntity {
        val noteText = "Note to test Sync"
        val noteEntity = NoteEntity(
            id = 0,
            noteText = noteText,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            isRemoved = isRemoved,
            syncId = syncId
        )

        val id = noteDao.upsert(noteEntity).first().toInt()

        return noteEntity.copy(id = id)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnSyncId_ReturnsEntity() = runTest {
        val dto = addDto(
            SyncEntity.generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addNoteEntity().copy(
            syncId = dto.id,
            noteText = "Not the Same text as Dto"
        )

        noteDao.upsert(entity)

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

        val entity = addNoteEntity().copy(
            noteText = dto.noteText,
            createdAt = Instant.parse(dto.createdAt).toEpochMilliseconds()
        )

        noteDao.upsert(entity)

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

        val entity = addNoteEntity().copy(
            noteText = "Not the Same text as Dto"
        )

        noteDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertNull(lookupEntity)
    }
}