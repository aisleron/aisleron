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

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleDto
import com.aisleron.data.aisle.AisleDtoMapper
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationEntity
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AisleSyncTest : SyncTest<AisleEntity, AisleDto>() {
    private val aisleDao: AisleDao get() = dao as AisleDao

    override fun initSyncApi(): SyncApiTestImpl<AisleDto> =
        SyncApiTestImpl("aisles")

    override fun initMapper(): DtoMapper<AisleEntity, AisleDto> =
        AisleDtoMapper(aisleDao, get<LocationDao>())

    override fun initDao(): SyncDao<AisleEntity> =
        get<AisleDao>()

    override suspend fun addEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String?
    ): AisleEntity = addAisleEntity(lastModifiedAt, serverUpdatedAt, isRemoved, syncId)

    private suspend fun addAisleEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String? = null,
        withLocation: Boolean = true
    ): AisleEntity {
        val locationId = if (withLocation)
            addLocationEntity().id
        else
            0

        val entity = AisleEntity(
            id = 0,
            name = "Aisle for Sync Test",
            expanded = true,
            rank = 1,
            isRemoved = isRemoved,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            syncId = syncId,
            locationId = locationId
        )

        val id = aisleDao.upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    override suspend fun addDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): AisleDto = addAisleDto(id, serverUpdatedAt, clientUpdatedAt, isDeleted)

    private suspend fun addAisleDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean,
        withLocation: Boolean = true
    ): AisleDto {
        val locationId = if (withLocation)
            addLocationEntity().syncId!!
        else
            ""

        val dto = AisleDto(
            id = id,
            isDeleted = isDeleted,
            clientUpdatedAt = clientUpdatedAt,
            serverUpdatedAt = serverUpdatedAt,
            name = "Aisle for Sync Test",
            locationId = locationId,
            rank = 1,
            isDefault = false,
        )

        syncApi.push(listOf(dto))

        return dto
    }

    override suspend fun validateDtoToEntity(
        dto: AisleDto, compareEntity: AisleEntity
    ): Boolean {
        val expectedEntity = mapper.fromDto(dto).copy(
            id = compareEntity.id,
            expanded = compareEntity.expanded
        )

        return expectedEntity == compareEntity
    }

    private suspend fun addLocationEntity(): LocationEntity {
        val entity = LocationEntity(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "Location for Sync Test",
            pinned = true,
            showDefaultAisle = true,
            noteId = null,
            expanded = true,
            rank = 1,
            isRemoved = false,
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            syncId = generateSyncId()
        )

        val id = get<LocationDao>().upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    @Test
    fun toDto_LocationNotFound_ThrowsException() = runTest {
        val entity = addAisleEntity(0, 0, isRemoved = false, withLocation = false)

        assertFailsWith<IllegalStateException> {
            mapper.toDto(entity)
        }
    }

    @Test
    fun fromDto_LocationNotFound_ThrowsException() = runTest {
        val dto = addAisleDto(
            generateSyncId(), "", "",
            isDeleted = false,
            withLocation = false
        )

        assertFailsWith<IllegalStateException> {
            mapper.fromDto(dto)
        }
    }

    @Test
    fun fromDto_ExistingEntityProvided_EntityUpdated() = runTest {
        val syncId = generateSyncId()
        val existingEntity = addAisleEntity(
            lastModifiedAt = 100L,
            serverUpdatedAt = null,
            isRemoved = false,
            syncId = syncId
        )

        val dto = addAisleDto(
            syncId, "2026-08-18T00:00:00Z", "2026-08-18T05:00:00Z",
            isDeleted = false
        )

        val mappedEntity = mapper.fromDto(dto)

        assertEquals(existingEntity.id, mappedEntity.id)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnSyncId_ReturnsEntity() = runTest {
        val dto = addAisleDto(
            generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addAisleEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            syncId = dto.id,
            name = "Not the Same as Dto"
        )

        aisleDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertEquals(entity, lookupEntity)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnNaturalKey_ReturnsEntity() = runTest {
        val dto = addAisleDto(
            generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false,
            withLocation = true
        )

        val entity = addAisleEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            name = dto.name,
            locationId = get<LocationDao>().getBySyncId(dto.locationId)!!.id
        )

        aisleDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertEquals(entity, lookupEntity)
    }

    @Test
    fun lookupEntityFromDto_NoEntityMatch_ReturnsNull() = runTest {
        val dto = addAisleDto(
            generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addAisleEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            syncId = generateSyncId(),
            name = "Not the Same as Dto"
        )

        aisleDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertNull(lookupEntity)
    }
}