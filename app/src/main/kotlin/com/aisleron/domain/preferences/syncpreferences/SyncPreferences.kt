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

data class SyncPreferences(
    val syncServicePreference: SyncServicePreference,
    val serviceUrl: String,
    val serviceKey: String,
    val syncOnMobileData: Boolean,
    val lastSyncedAt: Long,
    val lastSyncStatus: SyncStatusPreference,
    val lastFailedReason: String,
    val remoteLastSyncedAt: Long
) {
    fun getRequiredNetworkConstraint(forceSync: Boolean) =
        when {
            syncServicePreference == SyncServicePreference.NONE -> SyncNetworkConstraint.NOT_REQUIRED
            syncOnMobileData || forceSync -> SyncNetworkConstraint.CONNECTED
            else -> SyncNetworkConstraint.UNMETERED
        }
}

enum class SyncNetworkConstraint {
    NOT_REQUIRED,
    CONNECTED,
    UNMETERED
}
