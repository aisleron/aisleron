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

import android.text.format.DateUtils
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.R
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.syncTestModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.SyncStatusPreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.testdata.data.preferences.syncpreferences.SyncPreferencesRepositoryTestImpl
import com.aisleron.testdata.data.sync.SyncSessionManagerTestImpl
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.ComposeScreenTest
import com.aisleron.ui.component.preference.labelRes
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AccountPreferencesScreenContentTest : ComposeScreenTest() {
    override val koinModules = listOf(
        useCaseModule, preferenceTestModule, generalTestModule, viewModelTestModule, syncTestModule
    )

    @Composable
    private fun SetAccountPreferencesScreenContent(
        state: AccountPreferencesUiState,
        snackbarHostState: SnackbarHostState? = SnackbarHostState(),
        onSaveSyncServiceAddress: (url: String, key: String) -> Unit = { _, _ -> },
        onSignInPressed: () -> Unit = {},
        onSignOutPressed: () -> Unit = {},
        onSyncOnMobileDataChanged: ((Boolean) -> Unit) = {},
        onSyncServiceChanged: ((SyncServicePreference) -> Unit) = {},
        onSyncStatusPressed: () -> Unit = {}
    ) {
        AccountPreferencesScreenContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onSaveSyncServiceAddress = onSaveSyncServiceAddress,
            onSignInPressed = onSignInPressed,
            onSignOutPressed = onSignOutPressed,
            onSyncOnMobileDataChanged = onSyncOnMobileDataChanged,
            onSyncServiceChanged = onSyncServiceChanged,
            onSyncStatusPressed = onSyncStatusPressed
        )
    }

    private fun getDefaultSyncPreferences() = SyncPreferences(
        syncServicePreference = SyncServicePreference.NONE,
        serviceUrl = "",
        serviceKey = "",
        syncOnMobileData = false,
        lastSyncedAt = 0L,
        lastSyncStatus = SyncStatusPreference.NONE,
        lastFailedReason = "",
        remoteLastSyncedAt = 0L
    )

    private fun getSyncPreferencesRepository() =
        get<SyncPreferencesRepository>() as SyncPreferencesRepositoryTestImpl

    @Test
    fun accountPreferences_OnSignOutFailure_DisplaysErrorSnackbar() = runKoinComposeUiTest {
        val exceptionCode = AisleronException.ExceptionCode.SIGN_OUT_EXCEPTION

        getSyncPreferencesRepository().setSyncPreferences(
            getDefaultSyncPreferences().copy(
                syncServicePreference = SyncServicePreference.CUSTOM_SERVICE,
                serviceUrl = "https://sync.aisleron.com",
                serviceKey = "example-key"
            )
        )

        val sessionManager = get<SyncSessionManager>() as SyncSessionManagerTestImpl
        sessionManager.failWith(AisleronException.AuthException(exceptionCode))
        sessionManager.setFutureStatus(SyncSessionStatus.Authenticated("test@example.com"))
        sessionManager.refreshStatus()

        setContent {
            AccountPreferencesScreen(onSignInPressed = {})
        }

        val signOutText = getString(R.string.sign_out)
        onNodeWithText(signOutText)
            .assertIsEnabled()
            .performClick()

        val expectedErrorText = getString(
            AisleronExceptionMap().getErrorResourceId(exceptionCode)
        )

        onNodeWithText(expectedErrorText).assertIsDisplayed()
    }

    @Test
    fun accountPreferences_OnSyncOnMobileDataChanged_PreferenceUpdated() = runKoinComposeUiTest {
        val syncOnMobileDataBefore = false

        val syncServicePreference = getSyncPreferencesRepository()
        syncServicePreference.setSyncPreferences(
            getDefaultSyncPreferences().copy(
                syncServicePreference = SyncServicePreference.CUSTOM_SERVICE,
                serviceUrl = "https://sync.aisleron.com",
                serviceKey = "example-key",
                syncOnMobileData = syncOnMobileDataBefore
            )
        )

        setContent {
            AccountPreferencesScreen(onSignInPressed = {})
        }

        val syncOnMobileDataText = getString(R.string.sync_on_mobile_data)
        onNodeWithText(syncOnMobileDataText).performClick()

        val syncOnMobileDataAfter = syncServicePreference.getSyncPreferences().syncOnMobileData
        assertTrue(syncOnMobileDataBefore != syncOnMobileDataAfter)
    }

    @Test
    fun accountPreferences_OnSyncServiceChanged_PreferenceUpdated() = runKoinComposeUiTest {
        val syncServiceBefore = SyncServicePreference.NONE

        val syncServicePreference = getSyncPreferencesRepository()
        syncServicePreference.setSyncPreferences(
            getDefaultSyncPreferences().copy(
                syncServicePreference = syncServiceBefore,
                serviceUrl = "https://sync.aisleron.com",
                serviceKey = "example-key"
            )
        )

        setContent {
            AccountPreferencesScreen(onSignInPressed = {})
        }

        val syncServiceText = getString(R.string.sync_service)
        onNodeWithText(syncServiceText).performClick()
        onNodeWithText(getString(R.string.sync_service_custom)).performClick()

        val syncServiceAfter = syncServicePreference.getSyncPreferences().syncServicePreference
        assertTrue(syncServiceBefore != syncServiceAfter)
        assertEquals(SyncServicePreference.CUSTOM_SERVICE, syncServiceAfter)
    }

    @Test
    fun accountPreferences_OnSyncStarted_ShowSyncStartedSnackbar() = runKoinComposeUiTest {
        getSyncPreferencesRepository().setSyncPreferences(
            getDefaultSyncPreferences().copy(
                syncServicePreference = SyncServicePreference.CUSTOM_SERVICE,
                serviceUrl = "https://sync.aisleron.com",
                serviceKey = "example-key"
            )
        )

        setContent {
            AccountPreferencesScreen(onSignInPressed = {})
        }

        val lastSyncText = getString(R.string.last_sync)
        onNodeWithText(lastSyncText)
            .assertIsEnabled()
            .performClick()

        val expectedSnackbarText = getString(R.string.manual_sync_scheduled)

        onNodeWithText(expectedSnackbarText).assertIsDisplayed()
    }

    @Test
    fun accountPreferencesContent_AuthenticatedState_DisplaysSignOutAndUserInfo() =
        runComposeUiTest {
            val user = "user@example.com"
            val signOutText = getString(R.string.sign_out)
            val statusText = getContext().getString(R.string.sync_session_signed_in_as, user)

            val state = AccountPreferencesUiState(
                serviceUrl = "https://sync.aisleron.com",
                sessionStatus = SyncSessionStatus.Authenticated(userId = user)
            )

            setContent {
                SetAccountPreferencesScreenContent(state = state)
            }

            onNodeWithText(signOutText).assertIsDisplayed()
            onNodeWithText(statusText, substring = true).assertIsDisplayed()
        }

    @Test
    fun accountPreferencesContent_UnconfiguredState_DisablesSignInOption() = runComposeUiTest {
        val signInText = getString(R.string.sign_in)

        val state = AccountPreferencesUiState(
            serviceUrl = "",
            sessionStatus = SyncSessionStatus.NotConfigured
        )

        setContent {
            SetAccountPreferencesScreenContent(state = state)
        }

        onNodeWithText(signInText).assertIsNotEnabled()
    }

    @Test
    fun accountPreferencesContent_ClickSignIn_InvokesSignInCallback() = runComposeUiTest {
        val signInText = getString(R.string.sign_in)
        var signInClicked = false
        val state = AccountPreferencesUiState(
            serviceUrl = "https://sync.aisleron.com",
            sessionStatus = SyncSessionStatus.NotAuthenticated
        )

        setContent {
            SetAccountPreferencesScreenContent(
                state = state,
                onSignInPressed = { signInClicked = true }
            )
        }

        onNodeWithText(signInText)
            .assertIsEnabled()
            .performClick()

        assertTrue(signInClicked)
    }

    @Test
    fun accountPreferencesContent_ToggleMobileData_InvokesMobileDataCallback() = runComposeUiTest {
        val syncMobileDataText = getString(R.string.sync_on_mobile_data)
        var updatedValue: Boolean? = null
        val state = AccountPreferencesUiState(
            syncOnMobileData = false
        )

        setContent {
            SetAccountPreferencesScreenContent(
                state = state,
                onSyncOnMobileDataChanged = { newValue -> updatedValue = newValue },
            )
        }

        onNodeWithText(syncMobileDataText)
            .performClick()

        assertEquals(true, updatedValue)
    }

    @Test
    fun accountPreferencesContent_ClickSyncServiceAddress_DisplaysSyncServiceDialog() =
        runComposeUiTest {
            val syncServiceAddressText = getString(R.string.sync_service_address)
            val dialogTitleText = getString(R.string.sync_service_title)
            val state = AccountPreferencesUiState(
                syncServicePreference = SyncServicePreference.CUSTOM_SERVICE
            )

            setContent {
                SetAccountPreferencesScreenContent(state = state)
            }

            onNodeWithText(syncServiceAddressText).performClick()

            onNodeWithText(dialogTitleText).assertIsDisplayed()
        }

    @Test
    fun accountPreferencesContent_ClickSignIn_TriggersSignInPressed() = runComposeUiTest {
        val signInText = getString(R.string.sign_in)
        var signInPressed = false
        val state = AccountPreferencesUiState(
            serviceUrl = "https://sync.aisleron.com",
            sessionStatus = SyncSessionStatus.NotAuthenticated
        )

        setContent {
            SetAccountPreferencesScreenContent(
                state = state,
                onSignInPressed = { signInPressed = true }
            )
        }

        onNodeWithText(signInText)
            .assertIsEnabled()
            .performClick()

        assertTrue(signInPressed)
    }

    @Test
    fun accountPreferencesContent_ConfirmSyncServiceDialog_InvokesSaveSyncServiceCallback() =
        runComposeUiTest {
            val syncServiceText = getString(R.string.sync_service_address)
            val confirmText = getString(R.string.save)
            val expectedUrl = "https://new-sync.aisleron.com"
            val expectedKey = "secret-key"

            var savedUrl: String? = null
            var savedKey: String? = null

            val state = AccountPreferencesUiState(
                serviceUrl = "https://old-sync.aisleron.com",
                syncServicePreference = SyncServicePreference.CUSTOM_SERVICE
            )

            setContent {
                SetAccountPreferencesScreenContent(
                    state = state,
                    onSaveSyncServiceAddress = { url, key ->
                        savedUrl = url
                        savedKey = key
                    }
                )
            }

            onNodeWithText(syncServiceText).performClick()

            onNodeWithText(getString(R.string.sync_service_address_title))
                .performTextReplacement(expectedUrl)

            onNodeWithText(getString(R.string.sync_service_public_key))
                .performTextReplacement(expectedKey)

            onNodeWithText(confirmText).performClick()

            assertEquals(expectedUrl, savedUrl)
            assertEquals(expectedKey, savedKey)
        }

    private fun validateStatusDescription(
        sessionStatus: SyncSessionStatus, expectedResId: Int
    ) = runComposeUiTest {
        val statusText = getString(expectedResId)

        val state = AccountPreferencesUiState(
            serviceUrl = "https://sync.aisleron.com",
            sessionStatus = sessionStatus
        )

        setContent {
            SetAccountPreferencesScreenContent(state = state)
        }

        onNodeWithText(statusText, substring = true).assertIsDisplayed()
    }

    @Test
    fun accountPreferencesContent_SyncStatusIsLoading_ShowLoadingState() {
        validateStatusDescription(
            SyncSessionStatus.Loading,
            R.string.loading
        )
    }

    @Test
    fun accountPreferencesContent_SyncStatusIsNotAuthenticated_ShowNotAuthenticatedState() {
        validateStatusDescription(
            SyncSessionStatus.NotAuthenticated,
            R.string.sync_session_not_authenticated
        )
    }

    @Test
    fun accountPreferencesContent_SyncStatusIsRefreshFailure_ShowRefreshFailureState() {
        validateStatusDescription(
            SyncSessionStatus.RefreshFailure,
            R.string.sync_session_refresh_failure
        )
    }

    @Test
    fun accountPreferencesContent_LoadingState_DisplaysLoadingIndicator() = runComposeUiTest {
        val state = AccountPreferencesUiState(
            isLoading = true
        )

        setContent {
            SetAccountPreferencesScreenContent(state = state)
        }

        // Match by ProgressBar role or content description / tag used for your loading indicator
        onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun accountPreferencesContent_SyncServiceIsNone_HideSyncServiceAddress() = runComposeUiTest {
        val syncServiceAddressText = getString(R.string.sync_service_address)
        val state = AccountPreferencesUiState(
            syncServicePreference = SyncServicePreference.NONE
        )

        setContent {
            SetAccountPreferencesScreenContent(state = state)
        }

        onNodeWithText(syncServiceAddressText).assertDoesNotExist()
    }

    @Test
    fun accountPreferencesContent_SyncServiceIsCustom_ShowSyncServiceAddress() = runComposeUiTest {
        val syncServiceAddressText = getString(R.string.sync_service_address)
        val state = AccountPreferencesUiState(
            syncServicePreference = SyncServicePreference.CUSTOM_SERVICE
        )

        setContent {
            SetAccountPreferencesScreenContent(state = state)
        }

        onNodeWithText(syncServiceAddressText).assertIsDisplayed()
    }

    @Test
    fun accountPreferencesContent_ClickLastSync_TriggersSyncStatusPressed() = runComposeUiTest {
        val lastSyncText = getString(R.string.last_sync)
        var lastSyncPressed = false
        val state = AccountPreferencesUiState(
            serviceUrl = "https://sync.aisleron.com",
            sessionStatus = SyncSessionStatus.NotAuthenticated
        )

        setContent {
            SetAccountPreferencesScreenContent(
                state = state,
                onSyncStatusPressed = { lastSyncPressed = true }
            )
        }

        onNodeWithText(lastSyncText)
            .assertIsEnabled()
            .performClick()

        assertTrue(lastSyncPressed)
    }

    @Test
    fun accountPreferencesContent_HasLastSyncStatus_ShowLastSyncStatus() = runComposeUiTest {
        val lastSyncDate = System.currentTimeMillis()
        val statusText = DateUtils.formatDateTime(
            getInstrumentation().targetContext,
            lastSyncDate,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
        ) + " - ${getString(SyncStatusPreference.SUCCESS.labelRes)}"

        val state = AccountPreferencesUiState(
            lastSyncDate = lastSyncDate,
            lastSyncStatus = SyncStatusPreference.SUCCESS
        )

        setContent {
            SetAccountPreferencesScreenContent(state = state)
        }

        onNodeWithText(statusText, substring = true).assertIsDisplayed()
    }

    @Test
    fun accountPreferencesContent_LastSyncFailed_ShowFailReason() = runComposeUiTest {
        val lastSyncDate = System.currentTimeMillis()
        val errorReason = "Big Error"
        val statusText = DateUtils.formatDateTime(
            getInstrumentation().targetContext,
            lastSyncDate,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
        ) + " - ${getString(SyncStatusPreference.FAILURE.labelRes, errorReason)}"

        val state = AccountPreferencesUiState(
            lastSyncDate = lastSyncDate,
            lastSyncStatus = SyncStatusPreference.FAILURE,
            lastFailedReason = errorReason
        )

        setContent {
            SetAccountPreferencesScreenContent(state = state)
        }

        onNodeWithText(statusText, substring = true).assertIsDisplayed()
    }

    @Test
    fun accountPreferencesContent_PressHelp_InvokesCallbackWithCorrectUrl() = runComposeUiTest {
        val helpLabelString = getString(R.string.help)
        var capturedUrl: String? = null

        val fakeUriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                capturedUrl = uri
            }
        }

        setContent {
            CompositionLocalProvider(LocalUriHandler provides fakeUriHandler) {
                SetAccountPreferencesScreenContent(state = AccountPreferencesUiState())
            }
        }

        onNodeWithText(helpLabelString)
            .performScrollTo()
            .performClick()

        val expectedUri = getString(R.string.aisleron_sync_help_url)
        assertEquals(expectedUri, capturedUrl)
    }
}