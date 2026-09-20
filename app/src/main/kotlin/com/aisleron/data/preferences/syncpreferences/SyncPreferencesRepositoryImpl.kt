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

package com.aisleron.data.preferences.syncpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.SyncStatusPreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository.Companion.REMOTE_ENTITY_LAST_UPDATED_FORMAT
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class SyncPreferencesRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val defaultUrl: String,
    private val defaultKey: String
) : SyncPreferencesRepository {
    private fun getSyncService(): SyncServicePreference {
        val value = sharedPreferences.getString(SyncPreferenceKey.SYNC_SERVICE.keyName, null)
        return SyncServicePreference.fromValue(value)
    }

    private fun getServiceUrl(): String {
        return when (getSyncService()) {
            SyncServicePreference.NONE -> ""
            SyncServicePreference.CUSTOM_SERVICE ->
                sharedPreferences.getString(
                    SyncPreferenceKey.CUSTOM_SERVICE_URL.keyName, ""
                ).orEmpty()
        }
    }

    private fun getServiceKey(): String {
        return when (getSyncService()) {
            SyncServicePreference.NONE -> ""
            SyncServicePreference.CUSTOM_SERVICE ->
                sharedPreferences.getString(
                    SyncPreferenceKey.CUSTOM_SERVICE_KEY.keyName, ""
                ).orEmpty()
        }
    }

    private fun getSyncOnMobileData(): Boolean =
        sharedPreferences.getBoolean(SyncPreferenceKey.SYNC_ON_MOBILE_DATA.keyName, false)

    private fun getLastSyncedAt(): Long =
        sharedPreferences.getLong(SyncPreferenceKey.LAST_SYNCED_AT.keyName, 0)

    private fun getLastSyncSuccess(): SyncStatusPreference {
        val value = sharedPreferences.getString(SyncPreferenceKey.LAST_SYNC_SUCCESS.keyName, null)
        return SyncStatusPreference.fromValue(value)
    }

    private fun getLastFailedReason(): String =
        sharedPreferences.getString(SyncPreferenceKey.LAST_FAILED_REASON.keyName, "").orEmpty()

    private fun getRemoteLastSyncedAt(): Long =
        // Default to -1 so initial push takes into account records with modified date = 0
        sharedPreferences.getLong(SyncPreferenceKey.REMOTE_LAST_SYNCED_AT.keyName, -1L)


    override fun getSyncPreferences(): SyncPreferences =
        SyncPreferences(
            syncServicePreference = getSyncService(),
            serviceUrl = getServiceUrl(),
            serviceKey = getServiceKey(),
            syncOnMobileData = getSyncOnMobileData(),
            lastSyncedAt = getLastSyncedAt(),
            lastSyncStatus = getLastSyncSuccess(),
            lastFailedReason = getLastFailedReason(),
            remoteLastSyncedAt = getRemoteLastSyncedAt()
        )

    override fun getSyncPreferencesFlow(): Flow<SyncPreferences> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in SyncPreferenceKey.ALL_KEYS) {
                trySend(getSyncPreferences())
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(getSyncPreferences())

        // Clean up listener when the flow collection is cancelled
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    override fun setCustomServiceDetails(url: String, key: String) {
        sharedPreferences.edit {
            putString(SyncPreferenceKey.CUSTOM_SERVICE_URL.keyName, url)
            putString(SyncPreferenceKey.CUSTOM_SERVICE_KEY.keyName, key)
        }
    }

    override fun setSyncOnMobileData(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SyncPreferenceKey.SYNC_ON_MOBILE_DATA.keyName, value)
        }
    }

    override fun setSyncService(value: SyncServicePreference) {
        sharedPreferences.edit {
            putString(SyncPreferenceKey.SYNC_SERVICE.keyName, value.value)
        }
    }

    override fun setSyncStatus(lastSyncedAt: Long, status: SyncStatusPreference) {
        sharedPreferences.edit {
            putLong(SyncPreferenceKey.LAST_SYNCED_AT.keyName, lastSyncedAt)
            putString(SyncPreferenceKey.LAST_SYNC_SUCCESS.keyName, status.value)
        }
    }

    override fun setSyncStatus(status: SyncStatusPreference) {
        sharedPreferences.edit {
            putString(SyncPreferenceKey.LAST_SYNC_SUCCESS.keyName, status.value)
        }
    }

    override fun setLastFailedReason(value: String?) {
        sharedPreferences.edit {
            putString(SyncPreferenceKey.LAST_FAILED_REASON.keyName, value)
        }
    }

    private fun remoteEntityLastUpdatedKeyName(entityName: String): String =
        REMOTE_ENTITY_LAST_UPDATED_FORMAT.format(entityName)

    override fun getRemoteEntityLastUpdatedIso(entityName: String): String =
        sharedPreferences.getString(remoteEntityLastUpdatedKeyName(entityName), "") ?: ""


    override fun setRemoteEntityLastUpdatedIso(entityName: String, serverLastUpdatedAtIso: String) {
        sharedPreferences.edit {
            putString(remoteEntityLastUpdatedKeyName(entityName), serverLastUpdatedAtIso)
        }
    }

    override fun setRemoteLastSyncedAt(remoteLastSyncedAt: Long) {
        sharedPreferences.edit {
            putLong(SyncPreferenceKey.REMOTE_LAST_SYNCED_AT.keyName, remoteLastSyncedAt)
        }
    }
}

enum class SyncPreferenceKey(val keyName: String) {
    SYNC_SERVICE("sync_service"),
    CUSTOM_SERVICE_URL("custom_service_url"),
    CUSTOM_SERVICE_KEY("custom_service_key"),
    SYNC_ON_MOBILE_DATA("sync_on_mobile_data"),
    LAST_SYNCED_AT("last_synced_at"),
    LAST_SYNC_SUCCESS("last_sync_success"),
    LAST_FAILED_REASON("last_failed_reason"),
    REMOTE_LAST_SYNCED_AT("last_remote_synced_at");

    companion object {
        val ALL_KEYS: Set<String> by lazy {
            entries.mapTo(HashSet()) { it.keyName }
        }
    }
}