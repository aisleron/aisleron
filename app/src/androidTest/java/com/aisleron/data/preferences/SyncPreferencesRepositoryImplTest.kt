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

package com.aisleron.data.preferences

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.data.preferences.syncpreferences.SyncPreferenceKey
import com.aisleron.data.preferences.syncpreferences.SyncPreferencesRepositoryImpl
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.SyncStatusPreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository.Companion.REMOTE_ENTITY_LAST_UPDATED_FORMAT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SyncPreferencesRepositoryImplTest {
    private lateinit var syncPreferencesRepository: SyncPreferencesRepository
    private lateinit var sharedPreferencesInitializer: SharedPreferencesInitializer

    private val defaultUrl = "https://SyncPreferencesRepositoryImplTest.com"
    private val defaultKey = "SyncPreferencesRepositoryImplTest"

    @Before
    fun setUp() {
        sharedPreferencesInitializer = SharedPreferencesInitializer()
        sharedPreferencesInitializer.clearPreferences()
        syncPreferencesRepository = SyncPreferencesRepositoryImpl(
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext),
            defaultUrl = defaultUrl,
            defaultKey = defaultKey
        )
    }

    @Test
    fun getSyncPreferences_ForGivenSyncService_ReturnsCorrectSyncServiceValue() {
        SyncServicePreference.entries.forEach { syncService ->
            sharedPreferencesInitializer.setSyncService(syncService)

            val syncPreferences = syncPreferencesRepository.getSyncPreferences()

            assertEquals(
                syncService,
                syncPreferences.syncServicePreference,
                "Failed for sync service: $syncService"
            )
        }
    }

    @Test
    fun setSyncService_ForGivenSyncService_SetsCorrectSyncServiceValue() {
        SyncServicePreference.entries.forEach { syncService ->
            syncPreferencesRepository.setSyncService(syncService)

            val pref = preferences().getString(
                SyncPreferenceKey.SYNC_SERVICE.keyName, ""
            )

            assertEquals(
                syncService.value, pref, "Failed for sync service: $syncService"
            )
        }
    }

    /* TODO: re-enable TRUE path once Aisleron Default sync is configured
    @Test
    fun getSyncPreferences_UseDefaultService_ReturnsDefaultServiceUrl() {
        val customServiceUrl = "https://a.custom.url"
        val customServiceKey = "123abc"

        sharedPreferencesInitializer.setUseDefaultSyncService(true)
        sharedPreferencesInitializer.setCustomSyncServiceUrl(customServiceUrl)
        sharedPreferencesInitializer.setCustomSyncServiceKey(customServiceKey)

        val syncPreferences = syncPreferencesRepository.getSyncPreferences()

        assertEquals(defaultUrl, syncPreferences.serviceUrl)
        assertEquals(defaultKey, syncPreferences.serviceKey)
    }
    */

    @Test
    fun getSyncPreferences_UseCustomService_ReturnsCustomServiceUrl() {
        val customServiceUrl = "https://a.custom.url"
        val customServiceKey = "123abc"

        sharedPreferencesInitializer.setSyncService(SyncServicePreference.CUSTOM_SERVICE)
        sharedPreferencesInitializer.setCustomSyncServiceUrl(customServiceUrl)
        sharedPreferencesInitializer.setCustomSyncServiceKey(customServiceKey)

        val syncPreferences = syncPreferencesRepository.getSyncPreferences()

        assertEquals(customServiceUrl, syncPreferences.serviceUrl)
        assertEquals(customServiceKey, syncPreferences.serviceKey)
    }

    @Test
    fun getSyncPreferences_UseCustomServiceWithNoCustomParameters_ReturnsEmptyServiceUrl() {
        sharedPreferencesInitializer.setSyncService(SyncServicePreference.CUSTOM_SERVICE)

        val syncPreferences = syncPreferencesRepository.getSyncPreferences()

        assertEquals("", syncPreferences.serviceUrl)
        assertEquals("", syncPreferences.serviceKey)
    }

    @Test
    fun getSyncPreferences_NoSyncService_ReturnsEmptyServiceUrl() {
        val customServiceUrl = "https://a.custom.url"
        val customServiceKey = "123abc"

        sharedPreferencesInitializer.setSyncService(SyncServicePreference.NONE)
        sharedPreferencesInitializer.setCustomSyncServiceUrl(customServiceUrl)
        sharedPreferencesInitializer.setCustomSyncServiceKey(customServiceKey)

        val syncPreferences = syncPreferencesRepository.getSyncPreferences()

        assertEquals("", syncPreferences.serviceUrl)
        assertEquals("", syncPreferences.serviceKey)
    }

    @Test
    fun getSyncPreferencesFlow_ReturnsPreferenceFlow() = runTest {
        val customServiceUrl = "https://a.custom.url"
        val customServiceKey = "123abc"

        sharedPreferencesInitializer.setSyncService(SyncServicePreference.CUSTOM_SERVICE)
        sharedPreferencesInitializer.setCustomSyncServiceUrl(customServiceUrl)
        sharedPreferencesInitializer.setCustomSyncServiceKey(customServiceKey)

        val syncPreferencesFlow = syncPreferencesRepository.getSyncPreferencesFlow()
        val syncPreferences = syncPreferencesFlow.first()

        assertEquals(customServiceUrl, syncPreferences.serviceUrl)
        assertEquals(customServiceKey, syncPreferences.serviceKey)
    }

    @Test
    fun getSyncPreferencesFlow_ValueUpdated_EmitsUpdatedValue() = runTest {
        sharedPreferencesInitializer.setSyncOnMobileData(false)

        val emissions = mutableListOf<SyncPreferences>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            syncPreferencesRepository.getSyncPreferencesFlow().collect { emissions.add(it) }
        }

        waitUntil { emissions.isNotEmpty() }
        assertEquals(1, emissions.size)
        assertEquals(false, emissions.last().syncOnMobileData)

        sharedPreferencesInitializer.setSyncOnMobileData(true)

        waitUntil { emissions.size == 2 }
        assertEquals(2, emissions.size)
        assertEquals(true, emissions.last().syncOnMobileData)
    }

    private suspend fun waitUntil(
        timeoutMs: Long = 1000,
        pollIntervalMs: Long = 10,
        condition: () -> Boolean
    ) {
        val startTime = System.currentTimeMillis()
        while (!condition()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw AssertionError("Timed out waiting for condition to become true.")
            }
            delay(pollIntervalMs.milliseconds)
        }
    }

    private fun preferences() =
        PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)

    @Test
    fun setCustomServiceDetails_DetailsProvided_SettingsUpdated() {
        sharedPreferencesInitializer.setSyncService(SyncServicePreference.CUSTOM_SERVICE)
        val customUrl = "https://a.custom.url"
        val customKey = "123abc"

        syncPreferencesRepository.setCustomServiceDetails(customUrl, customKey)

        val updatedUrl = preferences().getString(
            SyncPreferenceKey.CUSTOM_SERVICE_URL.keyName, ""
        )

        assertEquals(customUrl, updatedUrl)

        val updatedKey = preferences().getString(
            SyncPreferenceKey.CUSTOM_SERVICE_KEY.keyName, ""
        )

        assertEquals(customKey, updatedKey)
    }

    @Test
    fun setSyncOnMobileData_ValueProvided_SyncOnMobileDataPreferenceSet() {
        sharedPreferencesInitializer.setSyncOnMobileData(false)
        val valueBefore = syncPreferencesRepository.getSyncPreferences().syncOnMobileData

        syncPreferencesRepository.setSyncOnMobileData(!valueBefore)

        val valueAfter = syncPreferencesRepository.getSyncPreferences().syncOnMobileData
        assertTrue(valueBefore != valueAfter)
    }

    @Test
    fun setSyncStatus_SyncDateAndSyncStatus_SettingsUpdated() {
        sharedPreferencesInitializer.setLastSyncedAt(0)
        sharedPreferencesInitializer.setLastSyncSuccess(SyncStatusPreference.NONE)

        val syncedAt = 100L
        val syncSuccess = SyncStatusPreference.SUCCESS

        syncPreferencesRepository.setSyncStatus(syncedAt, syncSuccess)

        val updatedSyncedAt = preferences().getLong(
            SyncPreferenceKey.LAST_SYNCED_AT.keyName, 0
        )

        assertEquals(syncedAt, updatedSyncedAt)

        val updatedSyncSuccess = preferences().getString(
            SyncPreferenceKey.LAST_SYNC_SUCCESS.keyName,
            SyncStatusPreference.NONE.value
        )

        assertEquals(syncSuccess.value, updatedSyncSuccess)
    }

    @Test
    fun setSyncStatus_SyncStatus_SettingUpdated() {
        sharedPreferencesInitializer.setLastSyncSuccess(SyncStatusPreference.NONE)

        val syncSuccess = SyncStatusPreference.SUCCESS

        syncPreferencesRepository.setSyncStatus(syncSuccess)

        val updatedSyncSuccess = preferences().getString(
            SyncPreferenceKey.LAST_SYNC_SUCCESS.keyName,
            SyncStatusPreference.NONE.value
        )

        assertEquals(syncSuccess.value, updatedSyncSuccess)
    }

    @Test
    fun getRemoteEntityLastUpdatedIso_HasNoValue_ReturnsEmptyString() {
        val entity = "test_entity"

        val preferenceValue = syncPreferencesRepository.getRemoteEntityLastUpdatedIso(entity)

        assertTrue(preferenceValue.isBlank())
    }

    @Test
    fun getRemoteEntityLastUpdatedIso_HasValue_ReturnsValue() {
        val entity = "test_entity"
        val lastUpdatedIso = "2026-08-18T00:00:00Z"
        sharedPreferencesInitializer.setRemoteEntityLastUpdatedIso(entity, lastUpdatedIso)

        val preferenceValue = syncPreferencesRepository.getRemoteEntityLastUpdatedIso(entity)

        assertEquals(lastUpdatedIso, preferenceValue)
    }

    @Test
    fun setRemoteEntityLastUpdatedIso_SetsPreference() {
        val entity = "test_entity"
        val lastUpdatedIso = "2026-08-18T00:00:00Z"

        syncPreferencesRepository.setRemoteEntityLastUpdatedIso(entity, lastUpdatedIso)

        val updatedValue = preferences().getString(
            REMOTE_ENTITY_LAST_UPDATED_FORMAT.format(entity), ""
        )

        assertEquals(lastUpdatedIso, updatedValue)
    }

    @Test
    fun setRemoteLastSyncedAt_ValueProvided_RemoteLastSyncedAtPreferenceSet() {
        sharedPreferencesInitializer.setRemoteLastSyncedAt(0)
        val newValue = 1000L

        syncPreferencesRepository.setRemoteLastSyncedAt(newValue)

        val valueAfter = syncPreferencesRepository.getSyncPreferences().remoteLastSyncedAt
        assertEquals(newValue, valueAfter)
    }

    @Test
    fun setLastFailedReason_ValueProvided_LastFailedReasonPreferenceSet() {
        sharedPreferencesInitializer.setLastFailedReason("")
        val newValue = "Sync Failed For Reasons"

        syncPreferencesRepository.setLastFailedReason(newValue)

        val valueAfter = syncPreferencesRepository.getSyncPreferences().lastFailedReason
        assertEquals(newValue, valueAfter)
    }
}