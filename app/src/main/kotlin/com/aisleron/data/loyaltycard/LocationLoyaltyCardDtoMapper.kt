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

package com.aisleron.data.loyaltycard

import com.aisleron.data.location.LocationDao
import com.aisleron.data.sync.DtoMapper
import kotlin.time.Instant

class LocationLoyaltyCardDtoMapper(
    private val locationLoyaltyCardDao: LocationLoyaltyCardDao,
    private val locationDao: LocationDao,
    private val loyaltyCardDao: LoyaltyCardDao
) : DtoMapper<LocationLoyaltyCardEntity, LocationLoyaltyCardDto> {

    override suspend fun toDto(entity: LocationLoyaltyCardEntity): LocationLoyaltyCardDto {
        val locationSyncId =
            checkNotNull(locationDao.getLocation(entity.locationId, true)?.syncId) {
                "Location syncId not found for location ${entity.locationId}"
            }

        val loyaltyCardSyncId =
            checkNotNull(loyaltyCardDao.getLoyaltyCard(entity.loyaltyCardId, true)?.syncId) {
                "Loyalty Card syncId not found for loyalty card ${entity.loyaltyCardId}"
            }

        return LocationLoyaltyCardDto(
            id = entity.syncId.orEmpty(),
            isDeleted = entity.isRemoved,
            clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString(),
            locationId = locationSyncId,
            loyaltyCardId = loyaltyCardSyncId
        )
    }

    private suspend fun getLocalLocationId(dto: LocationLoyaltyCardDto): Int {
        return checkNotNull(locationDao.getBySyncId(dto.locationId)?.id) {
            "Local location not found for syncId ${dto.locationId}"
        }
    }

    private suspend fun getLocalLoyaltyCardId(dto: LocationLoyaltyCardDto): Int {
        return checkNotNull(loyaltyCardDao.getBySyncId(dto.loyaltyCardId)?.id) {
            "Local loyalty card not found for syncId ${dto.loyaltyCardId}"
        }
    }

    override suspend fun fromDto(dto: LocationLoyaltyCardDto): LocationLoyaltyCardEntity {
        val localLocationId = getLocalLocationId(dto)
        val localLoyaltyCardId = getLocalLoyaltyCardId(dto)

        return LocationLoyaltyCardEntity(
            locationId = localLocationId,
            loyaltyCardId = localLoyaltyCardId,
            syncId = dto.id,
            isRemoved = dto.isDeleted,
            lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
            serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() }
        )
    }

    override suspend fun lookupEntityFromDto(dto: LocationLoyaltyCardDto): LocationLoyaltyCardEntity? {
        locationLoyaltyCardDao.getBySyncId(dto.id)?.let { return it }

        val localLocationId = getLocalLocationId(dto)
        val localLoyaltyCardId = getLocalLoyaltyCardId(dto)
        val entityList = locationLoyaltyCardDao.getByNaturalKey(localLocationId, localLoyaltyCardId)
            .filter { it.syncId == null }

        return entityList.firstOrNull { !it.isRemoved } ?: entityList.firstOrNull()
    }
}