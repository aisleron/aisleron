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

import com.aisleron.data.loyaltycard.LoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardEntity
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType

class LoyaltyCardDaoTestImpl : LoyaltyCardDao {
    override suspend fun getLoyaltyCard(loyaltyCardId: Int): LoyaltyCardEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun getProviderCard(
        provider: LoyaltyCardProviderType, providerCardId: Int
    ): LoyaltyCardEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun getLoyaltyCards(): List<LoyaltyCardEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getLoyaltyCardForLocation(locationId: Int): LoyaltyCardEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(vararg entity: LoyaltyCardEntity): List<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(vararg entity: LoyaltyCardEntity) {
        TODO("Not yet implemented")
    }
}