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

package com.aisleron.testdata.data.preferences.syncpreferences

import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.SyncStatusPreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository.Companion.REMOTE_ENTITY_LAST_UPDATED_FORMAT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SyncPreferencesRepositoryTestImpl : SyncPreferencesRepository {
    private val _syncPreferences = MutableStateFlow(getDefaultSyncPreferences())

    private val _remoteEntityUpdated = mutableMapOf<String, String>()
    val remoteEntityUpdated = _remoteEntityUpdated.toMap()

    override fun getSyncPreferences(): SyncPreferences = _syncPreferences.value
    override fun getSyncPreferencesFlow(): Flow<SyncPreferences> = _syncPreferences.asStateFlow()

    override fun setCustomServiceDetails(url: String, key: String) {
        _syncPreferences.update {
            it.copy(
                serviceUrl = url,
                serviceKey = key
            )
        }
    }

    override fun setSyncOnMobileData(value: Boolean) {
        _syncPreferences.update {
            it.copy(syncOnMobileData = value)
        }
    }

    override fun setSyncService(value: SyncServicePreference) {
        _syncPreferences.update {
            it.copy(syncServicePreference = value)
        }
    }

    override fun setSyncStatus(lastSyncedAt: Long, status: SyncStatusPreference) {
        _syncPreferences.update {
            it.copy(
                lastSyncedAt = lastSyncedAt,
                lastSyncStatus = status
            )
        }
    }

    override fun setSyncStatus(status: SyncStatusPreference) {
        _syncPreferences.update {
            it.copy(lastSyncStatus = status)
        }
    }

    override fun getRemoteEntityLastUpdatedIso(entityName: String): String {
        val keyName = REMOTE_ENTITY_LAST_UPDATED_FORMAT.format(entityName)
        return _remoteEntityUpdated[keyName] ?: ""
    }

    override fun setRemoteEntityLastUpdatedIso(
        entityName: String,
        serverLastUpdatedAtIso: String
    ) {
        val keyName = REMOTE_ENTITY_LAST_UPDATED_FORMAT.format(entityName)
        _remoteEntityUpdated[keyName] = serverLastUpdatedAtIso
    }

    override fun setRemoteLastSyncedAt(remoteLastSyncedAt: Long) {
        _syncPreferences.update {
            it.copy(remoteLastSyncedAt = remoteLastSyncedAt)
        }
    }

    fun setSyncPreferences(syncPreferences: SyncPreferences) {
        _syncPreferences.update { syncPreferences }
    }

    fun getDefaultSyncPreferences() = SyncPreferences(
        syncServicePreference = SyncServicePreference.NONE,
        serviceUrl = "",
        serviceKey = "",
        syncOnMobileData = false,
        lastSyncedAt = 0L,
        lastSyncStatus = SyncStatusPreference.NONE,
        remoteLastSyncedAt = -1L
    )

    fun resetSyncPreferences() {
        _syncPreferences.update { getDefaultSyncPreferences() }
        _remoteEntityUpdated.clear()
    }
}