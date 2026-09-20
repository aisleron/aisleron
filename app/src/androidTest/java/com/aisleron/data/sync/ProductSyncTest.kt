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

import com.aisleron.data.note.NoteDao
import com.aisleron.data.note.NoteEntity
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductDto
import com.aisleron.data.product.ProductDtoMapper
import com.aisleron.data.product.ProductEntity
import com.aisleron.domain.preferences.TrackingMode
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductSyncTest : SyncTest<ProductEntity, ProductDto>() {
    private val productDao: ProductDao get() = dao as ProductDao

    override fun initSyncApi(): SyncApiTestImpl<ProductDto> =
        SyncApiTestImpl("products")

    override fun initMapper(): DtoMapper<ProductEntity, ProductDto> =
        ProductDtoMapper(productDao, get<NoteDao>())

    override fun initDao(): SyncDao<ProductEntity> =
        get<ProductDao>()

    override suspend fun addEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String?
    ): ProductEntity = addProductEntity(
        lastModifiedAt, serverUpdatedAt, isRemoved, false, syncId
    )

    private suspend fun addNoteEntity(syncId: String? = generateSyncId()): NoteEntity {
        val entity = NoteEntity(
            id = 0,
            noteText = "Test Note for Location Sync",
            syncId = syncId
        )

        val id = get<NoteDao>().upsert(entity).first().toInt()
        return entity.copy(id = id)
    }

    private suspend fun addProductEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        withNote: Boolean,
        syncId: String? = null
    ): ProductEntity {
        val noteId = if (withNote) addNoteEntity().id else null
        val entity = ProductEntity(
            id = 0,
            name = "Product for Sync Test",
            noteId = noteId,
            isRemoved = isRemoved,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            syncId = syncId,
            inStock = true,
            qtyNeeded = 0.0,
            qtyIncrement = 1.0,
            unitOfMeasure = "",
            trackingMode = TrackingMode.DEFAULT
        )

        val id = productDao.upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    override suspend fun addDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): ProductDto = addProductDto(id, serverUpdatedAt, clientUpdatedAt, isDeleted, false)

    private suspend fun addProductDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean,
        withNote: Boolean
    ): ProductDto {
        val noteId = if (withNote) addNoteEntity().syncId else null

        val dto = ProductDto(
            id = id,
            isDeleted = isDeleted,
            clientUpdatedAt = clientUpdatedAt,
            serverUpdatedAt = serverUpdatedAt,
            name = "Location for Sync Test",
            noteId = noteId,
            inStock = true,
            qtyNeeded = 0.0,
            qtyIncrement = 1.0,
            unitOfMeasure = "",
            trackingMode = TrackingMode.DEFAULT,
        )

        syncApi.push(listOf(dto))

        return dto
    }

    override suspend fun validateDtoToEntity(
        dto: ProductDto, compareEntity: ProductEntity
    ): Boolean {
        val expectedEntity = mapper.fromDto(dto).copy(
            id = compareEntity.id,
        )

        return expectedEntity == compareEntity
    }

    @Test
    fun toDto_EntityHasNote_DtoHasNote() = runTest {
        val entity = addProductEntity(100, 100, isRemoved = false, withNote = true)

        val dto = mapper.toDto(entity)

        val noteSyncId = get<NoteDao>().getNote(entity.noteId!!, false)?.syncId
        assertEquals(noteSyncId, dto.noteId)
    }

    @Test
    fun fromDto_DtoHasNote_EntityHasNote() = runTest {
        val dto = addProductDto(
            generateSyncId(), "2026-08-18T00:00:00Z", "2026-08-18T05:00:00Z",
            isDeleted = false,
            withNote = true
        )

        val entity = mapper.fromDto(dto)

        val noteId = get<NoteDao>().getBySyncId(dto.noteId!!)?.id
        assertEquals(noteId, entity.noteId)
    }

    @Test
    fun fromDto_ExistingEntityProvided_EntityUpdated() = runTest {
        val syncId = generateSyncId()
        val existingEntity = addProductEntity(
            lastModifiedAt = 100L,
            serverUpdatedAt = null,
            isRemoved = false,
            withNote = false,
            syncId = syncId
        )

        val dto = addProductDto(
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
            generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addProductEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false,
            withNote = false,
        ).copy(
            syncId = dto.id,
            name = "Not the Same as Dto"
        )

        productDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertEquals(entity, lookupEntity)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnNaturalKey_ReturnsEntity() = runTest {
        val dto = addDto(
            generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addProductEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false,
            withNote = false,
        ).copy(
            name = dto.name
        )

        productDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertEquals(entity, lookupEntity)
    }

    @Test
    fun lookupEntityFromDto_NoEntityMatch_ReturnsNull() = runTest {
        val dto = addDto(
            generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addProductEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false,
            withNote = false,
        ).copy(
            syncId = generateSyncId(),
            name = "Not the Same as Dto"
        )

        productDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertNull(lookupEntity)
    }
}