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

package com.aisleron.ui.account

import com.aisleron.data.sync.SyncSchedulerTestImpl
import com.aisleron.di.KoinTestRule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.syncTestModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.testdata.data.sync.SyncSessionManagerTestImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountPreferencesViewModelTest : KoinTest {
    private lateinit var viewModel: AccountPreferencesViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            useCaseModule, preferenceTestModule, generalTestModule, viewModelTestModule,
            syncTestModule
        )
    )

    @Before
    fun setUp() {
        viewModel = get<AccountPreferencesViewModel>()
    }

    private fun getSyncSessionManager(): SyncSessionManagerTestImpl =
        get<SyncSessionManager>() as SyncSessionManagerTestImpl

    @Test
    fun signOut_UseCaseResultIsSuccess_SessionSignedOut() = runTest {
        val sessionManager = getSyncSessionManager()
        sessionManager.setSignedInProvider(SyncSessionManagerTestImpl.SignedInProvider.EMAIL)

        viewModel.signOut()

        assertEquals(
            SyncSessionManagerTestImpl.SignedInProvider.NONE,
            sessionManager.signedInProvider
        )

        val uiState = viewModel.uiState
        assertFalse(uiState.value.isLoading)
    }

    @Test
    fun signOut_UseCaseResultIsFailure_AisleronErrorCodeEmitted() = runTest {
        val sessionManager = getSyncSessionManager()
        sessionManager.setSignedInProvider(SyncSessionManagerTestImpl.SignedInProvider.EMAIL)
        sessionManager.failWith(Exception())

        viewModel.signOut()

        assertEquals(
            SyncSessionManagerTestImpl.SignedInProvider.EMAIL,
            sessionManager.signedInProvider
        )

        val uiState = viewModel.uiState
        assertFalse(uiState.value.isLoading)

        val errorCode = viewModel.uiEvent.value?.consumeEvent()
        assertIs<AccountPreferencesViewModel.UiEffect.SignOutFailure>(errorCode)

        val reconsumedEvent = viewModel.uiEvent.value?.consumeEvent()
        assertNull(reconsumedEvent)
    }

    @Test
    fun signOut_CancellationException_SignOutFailsWithNoError() = runTest {
        val sessionManager = getSyncSessionManager()
        sessionManager.setSignedInProvider(SyncSessionManagerTestImpl.SignedInProvider.EMAIL)
        sessionManager.failWith(CancellationException())

        viewModel.signOut()

        assertEquals(
            SyncSessionManagerTestImpl.SignedInProvider.EMAIL,
            sessionManager.signedInProvider
        )

        val errorState = viewModel.uiEvent
        assertNull(errorState.value)

        val uiState = viewModel.uiState
        assertFalse(uiState.value.isLoading)
    }

    @Test
    fun saveSyncServiceDetails_WithValidValues_PreferencesUpdated() = runTest {
        val syncPreferencesRepository = get<SyncPreferencesRepository>()
        syncPreferencesRepository.setCustomServiceDetails("", "")
        val url = "http://customurl.test"
        val key = "customKeyTest"

        viewModel.saveSyncServiceDetails(url, key)

        val preferences = syncPreferencesRepository.getSyncPreferences()
        assertEquals(url, preferences.serviceUrl)
        assertEquals(key, preferences.serviceKey)

        val uiState = viewModel.uiState.first()
        assertEquals(url, uiState.serviceUrl)
    }

    @Test
    fun saveSyncServiceDetails_WithValidValues_SyncSessionStatusUpdated() = runTest {
        val url = "http://customurl.test"
        val key = "customKeyTest"
        val sessionManager = getSyncSessionManager()
        sessionManager.setFutureStatus(SyncSessionStatus.NotAuthenticated)
        assertIs<SyncSessionStatus.NotConfigured>(viewModel.uiState.first().sessionStatus)

        viewModel.saveSyncServiceDetails(url, key)

        assertIs<SyncSessionStatus.NotAuthenticated>(sessionManager.sessionStatus.first())

        val uiState = viewModel.uiState.first()
        assertIs<SyncSessionStatus.NotAuthenticated>(uiState.sessionStatus)
    }

    @Test
    fun setSyncOnMobileData_WithValidValues_PreferencesUpdated() = runTest {
        val syncPreferencesRepository = get<SyncPreferencesRepository>()
        syncPreferencesRepository.setSyncOnMobileData(false)

        viewModel.setSyncOnMobileData(true)

        val preferences = syncPreferencesRepository.getSyncPreferences()
        assertTrue(preferences.syncOnMobileData)

        val uiState = viewModel.uiState.first()
        assertTrue(uiState.syncOnMobileData)
    }

    @Test
    fun constructor_WithDefaultParameters_AccountPreferencesViewModelReturned() {
        val vm = AccountPreferencesViewModel(
            signOutUseCase = get(),
            getSyncPreferencesUseCase = get(),
            getSyncPreferencesFlowUseCase = get(),
            setCustomSyncServiceDetailsUseCase = get(),
            getSessionStatusUseCase = get(),
            refreshSessionStatusUseCase = get(),
            setSyncOnMobileDataUseCase = get(),
            setSyncServiceUseCase = get(),
            scheduleForceSyncUseCase = get(),
            logger = get()
        )

        assertNotNull(vm)
    }

    @Test
    fun sessionStatusFlow_IsLoading_LoadingStatusReturned() = runTest {
        val sessionManager = getSyncSessionManager()
        sessionManager.setFutureStatus(SyncSessionStatus.Loading)

        sessionManager.refreshStatus()

        val uiState = viewModel.uiState.first()
        assertIs<SyncSessionStatus.Loading>(uiState.sessionStatus)
    }

    @Test
    fun setSyncService_WithValidValues_PreferencesUpdated() = runTest {
        val syncPreferencesRepository = get<SyncPreferencesRepository>()
        val syncServiceBefore = SyncServicePreference.NONE
        syncPreferencesRepository.setSyncService(syncServiceBefore)

        viewModel.setSyncService(SyncServicePreference.CUSTOM_SERVICE)

        val preferences = syncPreferencesRepository.getSyncPreferences()
        assertEquals(
            SyncServicePreference.CUSTOM_SERVICE, preferences.syncServicePreference
        )

        val uiState = viewModel.uiState.first()
        assertEquals(SyncServicePreference.CUSTOM_SERVICE, uiState.syncServicePreference)
    }

    @Test
    fun syncNow_SchedulesOneOffForceSync() = runTest {
        viewModel.syncNow()

        val scheduleType = get<SyncSchedulerTestImpl>().scheduleType
        assertEquals(SyncSchedulerTestImpl.ScheduleType.ONE_OFF_FORCE_SYNC, scheduleType)
    }
}