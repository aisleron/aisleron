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

import com.aisleron.data.loyaltycard.LoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardDto
import com.aisleron.data.loyaltycard.LoyaltyCardDtoMapper
import com.aisleron.data.loyaltycard.LoyaltyCardEntity
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoyaltyCardSyncTest : SyncTest<LoyaltyCardEntity, LoyaltyCardDto>() {
    private val loyaltyCardDao: LoyaltyCardDao get() = dao as LoyaltyCardDao

    override fun initSyncApi(): SyncApiTestImpl<LoyaltyCardDto> =
        SyncApiTestImpl("loyalty_cards")

    override fun initMapper(): DtoMapper<LoyaltyCardEntity, LoyaltyCardDto> =
        LoyaltyCardDtoMapper(loyaltyCardDao)

    override fun initDao(): SyncDao<LoyaltyCardEntity> =
        get<LoyaltyCardDao>()

    override suspend fun addEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String?
    ): LoyaltyCardEntity = addLoyaltyCardEntity(
        lastModifiedAt, serverUpdatedAt, isRemoved, syncId
    )

    private suspend fun addLoyaltyCardEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String? = null
    ): LoyaltyCardEntity {
        val entity = LoyaltyCardEntity(
            id = 0,
            name = "Loyalty Card for Sync Test",
            isRemoved = isRemoved,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            syncId = syncId,
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Intent for Sync Test",
        )

        val id = loyaltyCardDao.upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    override suspend fun addDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): LoyaltyCardDto = addLoyaltyCardDto(id, serverUpdatedAt, clientUpdatedAt, isDeleted)

    private suspend fun addLoyaltyCardDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): LoyaltyCardDto {
        val dto = LoyaltyCardDto(
            id = id,
            isDeleted = isDeleted,
            clientUpdatedAt = clientUpdatedAt,
            serverUpdatedAt = serverUpdatedAt,
            name = "Loyalty Card for Sync Test",
            provider = LoyaltyCardProviderType.CATIMA.name,
            intent = "Intent for Sync Test",
        )

        syncApi.push(listOf(dto))

        return dto
    }

    override suspend fun validateDtoToEntity(
        dto: LoyaltyCardDto, compareEntity: LoyaltyCardEntity
    ): Boolean {
        val expectedEntity = mapper.fromDto(dto).copy(
            id = compareEntity.id,
        )

        return expectedEntity == compareEntity
    }

    @Test
    fun fromDto_ExistingEntityProvided_EntityUpdated() = runTest {
        val syncId = generateSyncId()
        val existingEntity = addLoyaltyCardEntity(
            lastModifiedAt = 100L,
            serverUpdatedAt = null,
            isRemoved = false,
            syncId = syncId
        )

        val dto = addLoyaltyCardDto(
            syncId, "2026-08-18T00:00:00Z", "2026-08-18T05:00:00Z",
            isDeleted = false
        )

        val mappedEntity = mapper.fromDto(dto)

        assertEquals(existingEntity.id, mappedEntity.id)
    }

    @Test
    fun toDto_SyncIdNull_DtoIdIsEmptyString() = runTest {
        val entity = addLoyaltyCardEntity(
            lastModifiedAt = 100L,
            serverUpdatedAt = null,
            isRemoved = false,
            syncId = null
        )

        val dto = mapper.toDto(entity)

        assertEquals("", dto.id)
    }

    @Test
    fun lookupEntityFromDto_EntityMatchesOnSyncId_ReturnsEntity() = runTest {
        val dto = addDto(
            generateSyncId(),
            "2026-08-18T05:00:00Z",
            "2026-08-18T05:00:00Z",
            false
        )

        val entity = addLoyaltyCardEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            syncId = dto.id,
            name = "Not the Same as Dto"
        )

        loyaltyCardDao.upsert(entity)

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

        val entity = addLoyaltyCardEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            provider = LoyaltyCardProviderType.valueOf(dto.provider),
            intent = dto.intent
        )

        loyaltyCardDao.upsert(entity)

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

        val entity = addLoyaltyCardEntity(
            lastModifiedAt = 0,
            serverUpdatedAt = 0,
            isRemoved = false
        ).copy(
            syncId = generateSyncId(),
            intent = "Not the Same as Dto"
        )

        loyaltyCardDao.upsert(entity)

        val lookupEntity = mapper.lookupEntityFromDto(dto)

        assertNull(lookupEntity)
    }
}