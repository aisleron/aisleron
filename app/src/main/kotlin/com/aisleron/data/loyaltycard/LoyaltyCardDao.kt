/*
 * Copyright (C) 2025-2026 aisleron.com
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

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType

@Dao
interface LoyaltyCardDao : BaseDao<LoyaltyCardEntity> {
    @Query("SELECT * FROM LoyaltyCard WHERE id = :loyaltyCardId AND (isRemoved = 0 OR :includeRemoved = 1)")
    suspend fun getLoyaltyCard(loyaltyCardId: Int, includeRemoved: Boolean): LoyaltyCardEntity?

    @Query("SELECT * FROM LoyaltyCard WHERE provider = :provider AND intent = :intent AND isRemoved = 0")
    suspend fun getProviderCard(
        provider: LoyaltyCardProviderType, intent: String
    ): LoyaltyCardEntity?

    @Query("SELECT * FROM LoyaltyCard WHERE isRemoved = 0")
    suspend fun getLoyaltyCards(): List<LoyaltyCardEntity>

    @Query(
        "SELECT * FROM LoyaltyCard WHERE isRemoved = 0 AND EXISTS (" +
                "SELECT NULL FROM LocationLoyaltyCard llc " +
                "WHERE locationId = :locationId AND id = loyaltyCardId AND llc.isRemoved = 0)"
    )
    suspend fun getLoyaltyCardForLocation(locationId: Int): LoyaltyCardEntity?

    @Query(
        "UPDATE LocationLoyaltyCard SET isRemoved = :isRemoved, lastModifiedAt = :lastModifiedAt " +
                "WHERE loyaltyCardId = :loyaltyCardId"
    )
    suspend fun toggleLocationLoyaltyCardRemove(
        loyaltyCardId: Int, isRemoved: Boolean, lastModifiedAt: Long
    )

    @Transaction
    suspend fun updateLoyaltyCardRemovedState(loyaltyCard: LoyaltyCardEntity) {
        toggleLocationLoyaltyCardRemove(
            loyaltyCard.id, loyaltyCard.isRemoved, loyaltyCard.lastModifiedAt
        )

        upsert(loyaltyCard)
    }
}