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

import android.content.res.Configuration
import android.text.format.DateUtils
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aisleron.R
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.component.AisleronScreen
import com.aisleron.ui.component.FullScreenProgressIndicator
import com.aisleron.ui.component.account.SyncServiceConfigDialog
import com.aisleron.ui.component.preference.ListPreference
import com.aisleron.ui.component.preference.Preference
import com.aisleron.ui.component.preference.PreferenceCategory
import com.aisleron.ui.component.preference.SwitchPreference
import com.aisleron.ui.component.preference.UrlPreference
import com.aisleron.ui.component.preference.labelRes
import com.aisleron.ui.theme.AisleronTheme
import org.koin.androidx.compose.koinViewModel

@Composable
private fun getSessionStatusDescription(sessionStatus: SyncSessionStatus): String =
    when (sessionStatus) {
        is SyncSessionStatus.Authenticated ->
            stringResource(R.string.sync_session_signed_in_as, sessionStatus.userId)

        SyncSessionStatus.NotConfigured -> stringResource(R.string.sync_session_not_configured)
        SyncSessionStatus.Loading -> stringResource(R.string.loading)
        SyncSessionStatus.NotAuthenticated -> stringResource(R.string.sync_session_not_authenticated)
        SyncSessionStatus.RefreshFailure -> stringResource(R.string.sync_session_refresh_failure)
    }

@Composable
fun AccountPreferencesScreen(
    onSignInPressed: () -> Unit,
    viewModel: AccountPreferencesViewModel = koinViewModel()
) {
    val resources = LocalResources.current
    val exceptionMap = remember { AisleronExceptionMap() }
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEvent.collectAsStateWithLifecycle()

    LaunchedEffect(uiEvent) {
        uiEvent?.consumeEvent()?.let { effect ->
            when (effect) {
                is AccountPreferencesViewModel.UiEffect.SyncScheduled -> {
                    val message = resources.getString(R.string.manual_sync_scheduled)
                    snackbarHostState.showSnackbar(message = message)
                }

                is AccountPreferencesViewModel.UiEffect.SignOutFailure -> {
                    val resId = exceptionMap.getErrorResourceId(effect.errorCode)
                    val message = resources.getString(resId)
                    snackbarHostState.showSnackbar(message = message)
                    // TODO : Error snackbar formatting
                }
            }
        }
    }

    AccountPreferencesScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onSaveSyncServiceAddress = { url, key -> viewModel.saveSyncServiceDetails(url, key) },
        onSignInPressed = onSignInPressed,
        onSignOutPressed = { viewModel.signOut() },
        onSyncOnMobileDataChanged = { syncOnMobileData ->
            viewModel.setSyncOnMobileData(syncOnMobileData)
        },

        onSyncServiceChanged = { syncService ->
            viewModel.setSyncService(syncService)
        },

        onSyncStatusPressed = { viewModel.syncNow() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPreferencesScreenContent(
    state: AccountPreferencesUiState,
    snackbarHostState: SnackbarHostState?,
    onSaveSyncServiceAddress: (url: String, key: String) -> Unit,
    onSignInPressed: () -> Unit,
    onSignOutPressed: () -> Unit,
    onSyncOnMobileDataChanged: ((Boolean) -> Unit),
    onSyncServiceChanged: ((SyncServicePreference) -> Unit),
    onSyncStatusPressed: () -> Unit
) {
    var showSyncDialog by rememberSaveable { mutableStateOf(false) }

    AisleronScreen(
        title = stringResource(R.string.account_sync),
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            item {
                PreferenceCategory(title = stringResource(R.string.sync_settings)) {
                    ListPreference(
                        title = stringResource(R.string.sync_service),
                        selectedValue = state.syncServicePreference,
                        entries = SyncServicePreference.entries,
                        onValueSelected = onSyncServiceChanged
                    )

                    if (state.syncServicePreference == SyncServicePreference.CUSTOM_SERVICE) {
                        Preference(
                            title = stringResource(R.string.sync_service_address),
                            summary = state.serviceUrl.ifBlank { stringResource(R.string.preference_none) },
                            onClick = { showSyncDialog = true },
                            control = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    if (state.sessionStatus is SyncSessionStatus.Authenticated)
                        Preference(
                            title = stringResource(R.string.sign_out),
                            summary = getSessionStatusDescription(state.sessionStatus),
                            onClick = { onSignOutPressed() }
                        )
                    else
                        Preference(
                            title = stringResource(R.string.sign_in),
                            summary = getSessionStatusDescription(state.sessionStatus),
                            enabled = state.sessionStatus !is SyncSessionStatus.NotConfigured,
                            onClick = { onSignInPressed() },
                            control = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_right_24),
                                    contentDescription = null
                                )
                            }
                        )

                    val context = LocalContext.current
                    val lastSyncSummary = if (state.lastSyncDate == 0L)
                        stringResource(R.string.never)
                    else
                        DateUtils.formatDateTime(
                            context,
                            state.lastSyncDate,
                            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
                        ) + " - ${
                            stringResource(
                                state.lastSyncStatus.labelRes, state.lastFailedReason
                            )
                        }"

                    Preference(
                        title = stringResource(R.string.last_sync),
                        summary = lastSyncSummary,
                        summaryMaxLines = 3,
                        onClick = { onSyncStatusPressed() },
                        control = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_sync_24),
                                contentDescription = null
                            )
                        }
                    )

                    SwitchPreference(
                        title = stringResource(R.string.sync_on_mobile_data),
                        checked = state.syncOnMobileData,
                        onCheckedChanged = onSyncOnMobileDataChanged
                    )

                    UrlPreference(
                        iconResId = R.drawable.baseline_help_outline_24,
                        title = stringResource(R.string.help),
                        urlResId = R.string.aisleron_sync_help_url
                    )
                }
            }

            /*item {
                PreferenceCategory(title = stringResource(R.string.manage_account)) {
                    Preference(
                        title = "todo Subscription status",
                        summary = "todo Subscribed to 01 Jul 2027"
                    )

                    Preference("todo Change email qddress")
                    Preference("todo Change password")
                    Preference("todo Delete account")
                }
            }*/
        }
    }

    if (showSyncDialog) {
        SyncServiceConfigDialog(
            onDismissRequest = { showSyncDialog = false },
            onConfirmPressed = { url, key ->
                onSaveSyncServiceAddress(url, key)
                showSyncDialog = false
            },

            initialUrl = state.serviceUrl
        )
    }

    if (state.isLoading) {
        FullScreenProgressIndicator()
    }
}

@Preview(showSystemUi = true, name = "Account Preferences Screen Light Mode")
@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Account Preferences Dark Mode"
)
@Composable
fun AccountPreferencesScreenContentPreview() {
    val state = AccountPreferencesUiState(
        serviceUrl = "http://sunc.service:123",
        syncServicePreference = SyncServicePreference.CUSTOM_SERVICE
    )

    AisleronTheme {
        AccountPreferencesScreenContent(
            state = state,
            snackbarHostState = null,
            onSaveSyncServiceAddress = { _, _ -> },
            onSignInPressed = {},
            onSignOutPressed = {},
            onSyncOnMobileDataChanged = {},
            onSyncServiceChanged = {},
            onSyncStatusPressed = {}
        )
    }
}