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
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductEntity
import com.aisleron.data.productvariant.ProductVariantDao
import com.aisleron.data.productvariant.ProductVariantDto
import com.aisleron.data.productvariant.ProductVariantDtoMapper
import com.aisleron.data.productvariant.ProductVariantEntity
import com.aisleron.domain.preferences.TrackingMode
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ProductVariantSyncTest : SyncTest<ProductVariantEntity, ProductVariantDto>() {
    private val productVariantDao: ProductVariantDao get() = dao as ProductVariantDao

    override fun initSyncApi(): SyncApiTestImpl<ProductVariantDto> =
        SyncApiTestImpl("product_variants")

    override fun initMapper(): DtoMapper<ProductVariantEntity, ProductVariantDto> =
        ProductVariantDtoMapper(productVariantDao, get<ProductDao>())

    override fun initDao(): SyncDao<ProductVariantEntity> =
        get<ProductVariantDao>()

    override suspend fun addEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String?
    ): ProductVariantEntity =
        addProductVariantEntity(lastModifiedAt, serverUpdatedAt, isRemoved, syncId)

    private suspend fun addProductVariantEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String? = null
    ): ProductVariantEntity {
        val productId = addProductEntity(SyncEntity.generateSyncId()).id

        val entity = ProductVariantEntity(
            id = 0,
            isRemoved = isRemoved,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            syncId = syncId,
            productId = productId,
            barcode = "1234",
            createdAt = 0,
        )

        val id = productVariantDao.upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    override suspend fun addDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): ProductVariantDto = addProductVariantDto(id, serverUpdatedAt, clientUpdatedAt, isDeleted)

    private suspend fun addProductVariantDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): ProductVariantDto {
        val productId = addProductEntity(SyncEntity.generateSyncId()).syncId!!

        val dto = ProductVariantDto(
            id = id,
            isDeleted = isDeleted,
            clientUpdatedAt = clientUpdatedAt,
            serverUpdatedAt = serverUpdatedAt,
            productId = productId,
            barcode = "1234",
            createdAt = "2026-08-18T00:00:00Z",
        )

        syncApi.push(listOf(dto))

        return dto
    }

    override suspend fun validateDtoToEntity(
        dto: ProductVariantDto, compareEntity: ProductVariantEntity
    ): Boolean {
        val expectedEntity = mapper.fromDto(dto).copy(
            id = compareEntity.id
        )

        return expectedEntity == compareEntity
    }

    private suspend fun addProductEntity(syncId: String? = null): ProductEntity {
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
    fun toDto_ProductNotFound_ThrowsException() = runTest {
        val entity = addProductVariantEntity(0, 0, isRemoved = false)
            .copy(productId = -1)

        assertFailsWith<IllegalStateException> {
            mapper.toDto(entity)
        }
    }

    @Test
    fun fromDto_productNotFound_ThrowsException() = runTest {
        val dto = addProductVariantDto(
            SyncEntity.generateSyncId(), "", "",
            isDeleted = false
        ).copy(productId = SyncEntity.generateSyncId())

        assertFailsWith<IllegalStateException> {
            mapper.fromDto(dto)
        }
    }

    @Test
    fun fromDto_ExistingEntityProvided_EntityUpdated() = runTest {
        val syncId = SyncEntity.generateSyncId()
        val existingEntity = addProductVariantEntity(
            lastModifiedAt = 100L,
            serverUpdatedAt = null,
            isRemoved = false,
            syncId = syncId
        )

        val dto = addProductVariantDto(
            syncId, "2026-08-18T00:00:00Z", "2026-08-18T05:00:00Z",
            isDeleted = false
        )

        val mappedEntity = mapper.fromDto(dto)

        assertEquals(existingEntity.id, mappedEntity.id)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnSyncId_ReturnsEntity() = runTest {
        val dto = addProductVariantDto(
            SyncEntity.generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addProductVariantEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            syncId = dto.id,
            barcode = "Not the Same as Dto"
        )

        productVariantDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertEquals(entity, lookupEntity)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnNaturalKey_ReturnsEntity() = runTest {
        val dto = addProductVariantDto(
            SyncEntity.generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false,
        )

        val entity = addProductVariantEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            barcode = dto.barcode
        )

        productVariantDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertEquals(entity, lookupEntity)
    }

    @Test
    fun lookupEntityFromDto_NoEntityMatch_ReturnsNull() = runTest {
        val dto = addProductVariantDto(
            SyncEntity.generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addProductVariantEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            syncId = SyncEntity.generateSyncId(),
            barcode = "Not the Same as Dto"
        )

        productVariantDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertNull(lookupEntity)
    }
}