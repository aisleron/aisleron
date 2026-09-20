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

package com.aisleron.domain.preferences.syncpreferences

import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.SyncStatusPreference
import kotlinx.coroutines.flow.Flow

interface SyncPreferencesRepository {
    fun getSyncPreferences(): SyncPreferences
    fun getSyncPreferencesFlow(): Flow<SyncPreferences>
    fun setCustomServiceDetails(url: String, key: String)
    fun setSyncOnMobileData(value: Boolean)
    fun setSyncService(value: SyncServicePreference)
    fun setSyncStatus(lastSyncedAt: Long, status: SyncStatusPreference)
    fun setSyncStatus(status: SyncStatusPreference)
    fun setLastFailedReason(value: String?)
    fun getRemoteEntityLastUpdatedIso(entityName: String): String
    fun setRemoteEntityLastUpdatedIso(entityName: String, serverLastUpdatedAtIso: String)
    fun setRemoteLastSyncedAt(remoteLastSyncedAt: Long)

    companion object {
        const val REMOTE_ENTITY_LAST_UPDATED_FORMAT = "remote_%s_last_updated"
    }
}