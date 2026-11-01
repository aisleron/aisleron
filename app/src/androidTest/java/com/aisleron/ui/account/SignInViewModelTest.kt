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

import com.aisleron.di.KoinTestRule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.testdata.data.sync.SyncSessionManagerTestImpl
import kotlinx.coroutines.CancellationException
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

class SignInViewModelTest : KoinTest {
    private lateinit var viewModel: SignInViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            useCaseModule, preferenceTestModule, generalTestModule, viewModelTestModule
        )
    )

    @Before
    fun setUp() {
        viewModel = get<SignInViewModel>()
    }

    private fun getSyncSessionManager(): SyncSessionManagerTestImpl =
        get<SyncSessionManager>() as SyncSessionManagerTestImpl

    @Test
    fun signInWithEmail_UseCaseResultIsSuccess_SignInSuccessEmitted() = runTest {
        val sessionManager = getSyncSessionManager()
        sessionManager.setSignedIn(false)

        viewModel.signInWithEmail("email", "password")

        assertTrue(sessionManager.signedIn)

        val isLoading = viewModel.isLoading
        assertFalse(isLoading.value)

        val event = viewModel.signInEvent.value?.consumeEvent()
        assertIs<SignInViewModel.UiEffect.SignInSuccess>(event)

        val reconsumedEvent = viewModel.signInEvent.value?.consumeEvent()
        assertNull(reconsumedEvent)
    }

    @Test
    fun signInWithEmail_UseCaseResultIsFailure_SignInFailureEmitted() = runTest {
        val sessionManager = getSyncSessionManager()
        sessionManager.setSignedIn(false)
        sessionManager.failWith(Exception())

        viewModel.signInWithEmail("email", "password")

        assertFalse(sessionManager.signedIn)

        val isLoading = viewModel.isLoading
        assertFalse(isLoading.value)

        val event = viewModel.signInEvent.value?.consumeEvent()
        assertIs<SignInViewModel.UiEffect.SignInFailure>(event)

        val reconsumedEvent = viewModel.signInEvent.value?.consumeEvent()
        assertNull(reconsumedEvent)
    }

    @Test
    fun signInWithEmail_CancellationException_SignOutFailsWithNoError() = runTest {
        val sessionManager = getSyncSessionManager()
        sessionManager.setSignedIn(false)
        sessionManager.failWith(CancellationException())

        viewModel.signInWithEmail("email", "password")

        assertFalse(sessionManager.signedIn)

        val event = viewModel.signInEvent.value?.consumeEvent()
        assertNull(event)

        val isLoading = viewModel.isLoading
        assertFalse(isLoading.value)
    }

    @Test
    fun constructor_WithDefaultParameters_SignInViewModelReturned() {
        val vm = SignInViewModel(
            signInWithEmailUseCase = get(),
            getSyncPreferencesUseCase = get(),
            logger = get()
        )

        assertNotNull(vm)
    }

    @Test
    fun syncServiceUrl_get_ReturnsFromPreferences() {
        val url = "http://customurl.test"
        val syncPreferencesRepository = get<SyncPreferencesRepository>()
        syncPreferencesRepository.setCustomServiceDetails(url, "")

        val syncServiceUrl = viewModel.syncServiceUrl

        assertEquals(url, syncServiceUrl)
    }

    @Test
    fun syncServicePreference_get_ReturnsFromPreferences() {
        val service = SyncServicePreference.DEFAULT_SERVICE
        val syncPreferencesRepository = get<SyncPreferencesRepository>()
        syncPreferencesRepository.setSyncService(service)

        val syncServicePreference = viewModel.syncServicePreference

        assertEquals(service, syncServicePreference)
    }
}