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

import com.aisleron.domain.base.extension.runCatchingUnlessCancelled
import com.aisleron.domain.log.Logger
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.SyncStatusPreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncManager(
    repositories: List<SyncRepository>,
    private val syncPreferencesRepository: SyncPreferencesRepository,
    private val mutex: Mutex,
    private val logger: Logger
) {
    private val sortedRepositories = repositories.sortedBy { it.syncOrder }

    suspend fun syncAll(): Result<Unit> = mutex.withLock {
        val prefs = syncPreferencesRepository.getSyncPreferences()
        val syncStartTime = System.currentTimeMillis()

        return runCatchingUnlessCancelled {
            syncPreferencesRepository.setSyncStatus(SyncStatusPreference.RUNNING)
            if (prefs.syncServicePreference != SyncServicePreference.NONE) {
                val lastSyncedAt = prefs.remoteLastSyncedAt

                sortedRepositories.forEach {
                    logger.d(TAG, "Pushing ${it.remoteEntityName}")
                    it.push(lastSyncedAt)

                    val lastServerUpdatedDate =
                        syncPreferencesRepository.getRemoteEntityLastUpdatedIso(it.remoteEntityName)

                    logger.d(TAG, "Pulling ${it.remoteEntityName}")
                    val updatedServerUpdatedDate = it.pull(lastServerUpdatedDate)
                    syncPreferencesRepository.setRemoteEntityLastUpdatedIso(
                        it.remoteEntityName, updatedServerUpdatedDate
                    )
                }

                syncPreferencesRepository.setRemoteLastSyncedAt(syncStartTime)
            }

            sortedRepositories.forEach {
                logger.d(TAG, "Purging ${it.remoteEntityName}")
                it.purgeRemoved(syncStartTime)
            }
            syncPreferencesRepository.setSyncStatus(syncStartTime, SyncStatusPreference.SUCCESS)
        }.onFailure { throwable ->
            val failedMessage = throwable.message ?: "Sync operation failed"
            logger.e(TAG, failedMessage, throwable.cause)
            syncPreferencesRepository.setLastFailedReason(failedMessage)
            syncPreferencesRepository.setSyncStatus(syncStartTime, SyncStatusPreference.FAILURE)
        }
    }

    companion object {
        const val TAG = "SyncManager"
    }
}