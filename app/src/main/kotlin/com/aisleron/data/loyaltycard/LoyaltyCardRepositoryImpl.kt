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

import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.domain.loyaltycard.LoyaltyCardRepository

class LoyaltyCardRepositoryImpl(
    private val loyaltyCardDao: LoyaltyCardDao,
    private val locationLoyaltyCardDao: LocationLoyaltyCardDao,
    private val loyaltyCardMapper: LoyaltyCardMapper
) : LoyaltyCardRepository {
    override suspend fun get(id: Int): LoyaltyCard? =
        getLoyaltyCard(id, false)

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
        val locationLoyaltyCard = locationLoyaltyCardDao.getLocationLoyaltyCard(locationId, true)
            ?: LocationLoyaltyCardEntity(locationId, loyaltyCardId)

        val upsertEntry = locationLoyaltyCard.copy(
            loyaltyCardId = loyaltyCardId,
            isRemoved = false,
            lastModifiedAt = System.currentTimeMillis()
        )

        locationLoyaltyCardDao.upsert(upsertEntry)
    }

    override suspend fun removeFromLocation(locationId: Int, loyaltyCardId: Int) {
        val removeEntity = locationLoyaltyCardDao.getLocationLoyaltyCard(locationId, false)?.copy(
            isRemoved = true,
            lastModifiedAt = System.currentTimeMillis()
        )

        if (removeEntity != null && removeEntity.loyaltyCardId == loyaltyCardId) {
            locationLoyaltyCardDao.upsert(removeEntity)
        }
    }

    override suspend fun hardDeleteFromLocation(locationId: Int, loyaltyCardId: Int) {
        val deleteEntity = locationLoyaltyCardDao.getLocationLoyaltyCard(locationId, true)
        if (deleteEntity != null && deleteEntity.loyaltyCardId == loyaltyCardId) {
            locationLoyaltyCardDao.delete(deleteEntity)
        }
    }

    override suspend fun getAll(): List<LoyaltyCard> {
        return loyaltyCardMapper.toModelList(loyaltyCardDao.getLoyaltyCards())
    }

    override suspend fun add(item: LoyaltyCard): Int {
        return loyaltyCardDao.upsert(loyaltyCardMapper.fromModel(item, null)).single().toInt()
    }

    override suspend fun add(items: List<LoyaltyCard>): List<Int> {
        val loyaltyCards = items.map { loyaltyCardMapper.fromModel(it, null) }
        return upsertLoyaltyCards(loyaltyCards)
    }

    private suspend fun mapExisting(item: LoyaltyCard, includeRemoved: Boolean): LoyaltyCardEntity {
        val currentEntity = loyaltyCardDao.getLoyaltyCard(item.id, includeRemoved)
        return loyaltyCardMapper.fromModel(item, currentEntity)
    }

    override suspend fun update(item: LoyaltyCard) {
        loyaltyCardDao.upsert(mapExisting(item, false))
    }

    override suspend fun update(items: List<LoyaltyCard>) {
        val loyaltyCards = items.map { mapExisting(it, false) }
        upsertLoyaltyCards(loyaltyCards)
    }

    override suspend fun remove(item: LoyaltyCard) {
        val removeEntity = mapExisting(item, false).copy(isRemoved = true)
        loyaltyCardDao.updateLoyaltyCardRemovedState(removeEntity)
    }

    override suspend fun getRemoved(id: Int): LoyaltyCard? =
        getLoyaltyCard(id, true)

    override suspend fun restore(id: Int) {
        val removedEntity = loyaltyCardDao.getLoyaltyCard(id, true) ?: return

        val loyaltyCard = loyaltyCardMapper.toModel(removedEntity)
        val restoreEntity = loyaltyCardMapper.fromModel(loyaltyCard, removedEntity)
            .copy(isRemoved = false)

        loyaltyCardDao.updateLoyaltyCardRemovedState(restoreEntity)
    }

    override suspend fun hardDelete(item: LoyaltyCard) {
        val deleteEntity = mapExisting(item, true)
        loyaltyCardDao.delete(deleteEntity)
    }

    private suspend fun upsertLoyaltyCards(loyaltyCards: List<LoyaltyCardEntity>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return loyaltyCardDao
            .upsert(*loyaltyCards.toTypedArray())
            .map { it.toInt() }
    }

    private suspend fun getLoyaltyCard(id: Int, includeDeleted: Boolean): LoyaltyCard? {
        return loyaltyCardDao.getLoyaltyCard(id, includeDeleted)
            ?.let { loyaltyCardMapper.toModel(it) }
    }
}