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

package com.aisleron.data.loyaltycard

import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository

class LoyaltyCardRepositoryImpl(
    private val loyaltyCardDao: LoyaltyCardDao,
    private val locationLoyaltyCardDao: LocationLoyaltyCardDao,
    private val loyaltyCardMapper: LoyaltyCardMapper
) : LoyaltyCardRepository {
    override suspend fun get(id: Int): LoyaltyCard? {
        return loyaltyCardDao.getLoyaltyCard(id)?.let { loyaltyCardMapper.toModel(it) }
    }

    override suspend fun getProviderCard(
        provider: LoyaltyCardProviderType, intent: String
    ): LoyaltyCard? {
        return loyaltyCardDao.getProviderCard(provider, intent)
            ?.let { loyaltyCardMapper.toModel(it) }
    }

    override suspend fun getForLocation(locationId: Int): LoyaltyCard? {
        return loyaltyCardDao.getLoyaltyCardForLocation(locationId)
            ?.let { loyaltyCardMapper.toModel(it) }
    }

    override suspend fun addToLocation(locationId: Int, loyaltyCardId: Int) {
        locationLoyaltyCardDao.upsert(LocationLoyaltyCardEntity(locationId, loyaltyCardId))
    }

    override suspend fun removeFromLocation(locationId: Int, loyaltyCardId: Int) {
        locationLoyaltyCardDao.delete(LocationLoyaltyCardEntity(locationId, loyaltyCardId))
    }

    override suspend fun getAll(): List<LoyaltyCard> {
        return loyaltyCardMapper.toModelList(loyaltyCardDao.getLoyaltyCards())
    }

    override suspend fun add(item: LoyaltyCard): Int {
        return loyaltyCardDao.upsert(loyaltyCardMapper.fromModel(item)).single().toInt()
    }

    override suspend fun add(items: List<LoyaltyCard>): List<Int> {
        return upsertLoyaltyCards(items)
    }

    override suspend fun update(item: LoyaltyCard) {
        loyaltyCardDao.upsert(loyaltyCardMapper.fromModel(item))
    }

    override suspend fun update(items: List<LoyaltyCard>) {
        upsertLoyaltyCards(items)
    }

    override suspend fun remove(item: LoyaltyCard) {
        loyaltyCardDao.delete(loyaltyCardMapper.fromModel(item))
    }

    private suspend fun upsertLoyaltyCards(loyaltyCards: List<LoyaltyCard>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return loyaltyCardDao
            .upsert(*loyaltyCardMapper.fromModelList(loyaltyCards).map { it }.toTypedArray())
            .map { it.toInt() }
    }
}