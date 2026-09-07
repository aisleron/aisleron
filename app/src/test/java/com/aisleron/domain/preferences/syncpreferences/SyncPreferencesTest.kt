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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SyncPreferencesTest {
    private fun getSyncPreferences() = SyncPreferences(
        syncServicePreference = SyncServicePreference.CUSTOM_SERVICE,
        serviceUrl = "",
        serviceKey = "",
        syncOnMobileData = false,
        lastSyncedAt = 0L,
        lastSyncStatus = SyncStatusPreference.NONE,
        lastFailedReason = "",
        remoteLastSyncedAt = 0L
    )

    @Test
    fun getRequiredNetworkConstraint_NoSyncService_ReturnsNotRequired() {
        val syncPreferences = getSyncPreferences().copy(
            syncServicePreference = SyncServicePreference.NONE
        )

        val networkConstraint = syncPreferences.getRequiredNetworkConstraint(false)

        assertEquals(SyncNetworkConstraint.NOT_REQUIRED, networkConstraint)
    }

    @Test
    fun getRequiredNetworkConstraint_ForceSync_ReturnsConnected() {
        val syncPreferences = getSyncPreferences().copy(
            syncOnMobileData = false
        )

        val networkConstraint = syncPreferences.getRequiredNetworkConstraint(true)

        assertEquals(SyncNetworkConstraint.CONNECTED, networkConstraint)
    }

    @Test
    fun getRequiredNetworkConstraint_SyncOnMobileData_ReturnsConnected() {
        val syncPreferences = getSyncPreferences().copy(
            syncOnMobileData = true
        )

        val networkConstraint = syncPreferences.getRequiredNetworkConstraint(false)

        assertEquals(SyncNetworkConstraint.CONNECTED, networkConstraint)
    }

    @Test
    fun getRequiredNetworkConstraint_DoNotSyncOnMobileData_ReturnsUnmetered() {
        val syncPreferences = getSyncPreferences().copy(
            syncOnMobileData = false
        )

        val networkConstraint = syncPreferences.getRequiredNetworkConstraint(false)

        assertEquals(SyncNetworkConstraint.UNMETERED, networkConstraint)
    }
}