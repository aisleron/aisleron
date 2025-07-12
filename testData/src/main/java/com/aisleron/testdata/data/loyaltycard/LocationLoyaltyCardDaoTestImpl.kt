/*
 * Copyright (C) 2025 aisleron.com
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

package com.aisleron.testdata.data.loyaltycard

import com.aisleron.data.loyaltycard.LocationLoyaltyCardDao
import com.aisleron.data.loyaltycard.LocationLoyaltyCardEntity

class LocationLoyaltyCardDaoTestImpl : LocationLoyaltyCardDao {

    private val locationLoyaltyCardList = mutableListOf<LocationLoyaltyCardEntity>()

    fun getAll() = locationLoyaltyCardList

    fun getLocationLoyaltyCard(locationId: Int): LocationLoyaltyCardEntity? {
        return locationLoyaltyCardList.find { it.locationId == locationId }
    }


    override suspend fun upsert(vararg entity: LocationLoyaltyCardEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val existingEntity = getLocationLoyaltyCard(it.locationId)
            existingEntity?.let {
                locationLoyaltyCardList.removeAt(locationLoyaltyCardList.indexOf(existingEntity))
            }

            locationLoyaltyCardList.add(it)
            result.add(existingEntity?.let { -1 } ?: it.locationId.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: LocationLoyaltyCardEntity) {
        locationLoyaltyCardList.removeIf { it in entity }
    }
}