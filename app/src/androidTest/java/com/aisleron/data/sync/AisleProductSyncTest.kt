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
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.aisleproduct.AisleProductDto
import com.aisleron.data.aisleproduct.AisleProductDtoMapper
import com.aisleron.data.aisleproduct.AisleProductEntity
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductEntity
import com.aisleron.domain.preferences.TrackingMode
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AisleProductSyncTest : SyncTest<AisleProductEntity, AisleProductDto>() {
    private val aisleProductDao: AisleProductDao get() = dao as AisleProductDao

    override fun initSyncApi(): SyncApiTestImpl<AisleProductDto> =
        SyncApiTestImpl("aisle_products")

    override fun initMapper(): DtoMapper<AisleProductEntity, AisleProductDto> =
        AisleProductDtoMapper(aisleProductDao, get<AisleDao>(), get<ProductDao>())

    override fun initDao(): SyncDao<AisleProductEntity> =
        get<AisleProductDao>()

    override suspend fun addEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String?
    ): AisleProductEntity =
        addAisleProductEntity(lastModifiedAt, serverUpdatedAt, isRemoved, syncId)

    private suspend fun addAisleProductEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String? = null
    ): AisleProductEntity {
        val aisleId = addAisleEntity().id
        val productId = addProductEntity().id

        val entity = AisleProductEntity(
            id = 0,
            rank = 1,
            isRemoved = isRemoved,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            syncId = syncId,
            aisleId = aisleId,
            productId = productId,
        )

        val id = aisleProductDao.upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    override suspend fun addDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): AisleProductDto = addAisleProductDto(id, serverUpdatedAt, clientUpdatedAt, isDeleted)

    private suspend fun addAisleProductDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): AisleProductDto {
        val aisleId = addAisleEntity().syncId!!
        val productId = addProductEntity().syncId!!

        val dto = AisleProductDto(
            id = id,
            isDeleted = isDeleted,
            clientUpdatedAt = clientUpdatedAt,
            serverUpdatedAt = serverUpdatedAt,
            rank = 1,
            aisleId = aisleId,
            productId = productId,
        )

        syncApi.push(listOf(dto))

        return dto
    }

    override suspend fun validateDtoToEntity(
        dto: AisleProductDto, compareEntity: AisleProductEntity
    ): Boolean {
        val expectedEntity = mapper.fromDto(dto).copy(
            id = compareEntity.id
        )

        return expectedEntity == compareEntity
    }

    private suspend fun addAisleEntity(syncId: String? = generateSyncId()): AisleEntity {
        val entity = AisleEntity(
            id = 0,
            name = "Aisle for Sync Test",
            locationId = 1,
            rank = 1,
            isDefault = false,
            expanded = true,
            syncId = syncId,
        )

        val id = get<AisleDao>().upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    private suspend fun addProductEntity(syncId: String? = generateSyncId()): ProductEntity {
        val entity = ProductEntity(
            id = 0,
            name = "Product for Sync Test",
            syncId = syncId,
            inStock = true,
            qtyNeeded = 0.0,
            noteId = null,
            qtyIncrement = 1.0,
            unitOfMeasure = "",
            trackingMode = TrackingMode.DEFAULT
        )

        val id = get<ProductDao>().upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    @Test
    fun toDto_AisleNotFound_ThrowsException() = runTest {
        val entity = addAisleProductEntity(0, 0, isRemoved = false)
            .copy(aisleId = -1)

        assertFailsWith<IllegalStateException> {
            mapper.toDto(entity)
        }
    }

    @Test
    fun toDto_ProductNotFound_ThrowsException() = runTest {
        val entity = addAisleProductEntity(0, 0, isRemoved = false)
            .copy(productId = -1)

        assertFailsWith<IllegalStateException> {
            mapper.toDto(entity)
        }
    }

    @Test
    fun fromDto_aisleNotFound_ThrowsException() = runTest {
        val dto = addAisleProductDto(
            generateSyncId(), "", "",
            isDeleted = false
        ).copy(aisleId = generateSyncId())

        assertFailsWith<IllegalStateException> {
            mapper.fromDto(dto)
        }
    }

    @Test
    fun fromDto_productNotFound_ThrowsException() = runTest {
        val dto = addAisleProductDto(
            generateSyncId(), "", "",
            isDeleted = false
        ).copy(productId = generateSyncId())

        assertFailsWith<IllegalStateException> {
            mapper.fromDto(dto)
        }
    }

    @Test
    fun fromDto_ExistingEntityProvided_EntityUpdated() = runTest {
        val syncId = generateSyncId()
        val existingEntity = addAisleProductEntity(
            lastModifiedAt = 100L,
            serverUpdatedAt = null,
            isRemoved = false,
            syncId = syncId
        )

        val dto = addAisleProductDto(
            syncId, "2026-08-18T00:00:00Z", "2026-08-18T05:00:00Z",
            isDeleted = false
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

        val entity = addAisleProductEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false,
        ).copy(
            syncId = dto.id,
        )

        aisleProductDao.upsert(entity)

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

        val entity = addAisleProductEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            aisleId = get<AisleDao>().getBySyncId(dto.aisleId)!!.id,
            productId = get<ProductDao>().getBySyncId(dto.productId)!!.id
        )

        aisleProductDao.upsert(entity)

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

        val entity = addAisleProductEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false,
        ).copy(
            syncId = generateSyncId()
        )

        aisleProductDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertNull(lookupEntity)
    }

    @Test
    fun pull_HasDuplicateEntity_EntityReplaced() = runTest {
        // This test validates that the AisleProduct unique constraint is handled correctly by the
        // custom Dao upsert, since standard room upsert doesn't cater for unique keys
        val ap1 =
            addAisleProductEntity(0, 0, isRemoved = false, syncId = generateSyncId())

        val ap2 =
            addAisleProductEntity(0, 0, isRemoved = false, syncId = generateSyncId())

        assertEquals(2, aisleProductDao.getAisleProducts().size)

        val lastSyncIso = "2026-08-18T00:00:00Z"

        // Use sync id from ap1, and aisle & product details from ap2 to force a unique key violation
        val id = ap1.syncId!!
        val dto = addDto(id, "2026-08-18T05:00:00Z", "2026-08-17T05:00:00Z").copy(
            aisleId = get<AisleDao>().getAisle(ap2.id, false)!!.syncId!!,
            productId = get<ProductDao>().getProduct(ap2.id, false)!!.syncId!!,
        )

        syncApi.push(listOf(dto))

        repository.pull(lastSyncIso)

        // Assert that the clash has been resolved by replacing the conflicting record
        assertEquals(1, aisleProductDao.getAisleProducts().size)

        // Validate that the remaining entry matches the expected values
        val updatedEntity = aisleProductDao.getBySyncId(id)
        assertNotNull(updatedEntity)
        assertEquals(ap1.id, updatedEntity.id)
        assertEquals(ap2.aisleId, updatedEntity.aisleId)
        assertEquals(ap2.productId, updatedEntity.productId)
    }
}