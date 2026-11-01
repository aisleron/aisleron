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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aisleron.R
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.component.AisleronHeader
import com.aisleron.ui.component.AisleronScreen
import com.aisleron.ui.component.FullScreenProgressIndicator
import com.aisleron.ui.component.PasswordTextField
import com.aisleron.ui.component.preference.labelRes
import com.aisleron.ui.theme.AisleronTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    viewModel: SignInViewModel = koinViewModel()
) {
    val resources = LocalResources.current
    val uiEvent by viewModel.signInEvent.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val exceptionMap = remember { AisleronExceptionMap() }
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(uiEvent) {
        uiEvent?.consumeEvent()?.let { effect ->
            when (effect) {
                is SignInViewModel.UiEffect.SignInSuccess -> {
                    onSignInSuccess()
                }

                is SignInViewModel.UiEffect.SignInFailure -> {
                    val resId = exceptionMap.getErrorResourceId(effect.errorCode)
                    val message = resources.getString(resId)
                    snackbarHostState.showSnackbar(message = message)
                    // TODO : Error snackbar formatting
                }
            }
        }
    }

    SignInScreenContent(
        onSignInWithEmail = { email, password ->
            viewModel.signInWithEmail(
                email = email, password = password
            )
        },

        syncServicePreference = viewModel.syncServicePreference,
        syncServiceUrl = viewModel.syncServiceUrl,
        snackbarHostState = snackbarHostState,
        isLoading = isLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreenContent(
    onSignInWithEmail: (email: String, password: String) -> Unit,
    syncServicePreference: SyncServicePreference,
    syncServiceUrl: String,
    snackbarHostState: SnackbarHostState?,
    isLoading: Boolean
) {
    val scrollState = rememberScrollState()
    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()

    val padding16 = dimensionResource(id = R.dimen.padding_16)
    val padding2 = dimensionResource(id = R.dimen.padding_2)

    AisleronScreen(
        title = stringResource(R.string.sign_in_title),
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(padding16),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = padding16)
        ) {
            AisleronHeader(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.app_title),
                subtitle = stringResource(id = R.string.aisleron_sync_subtitle)
            )

            Text(
                text = stringResource(R.string.sync_service_sign_up_message),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(padding2)
            ) {
                Text(
                    text = stringResource(R.string.sync_service_connecting_to),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (syncServicePreference == SyncServicePreference.CUSTOM_SERVICE)
                        syncServiceUrl
                    else
                        stringResource(syncServicePreference.labelRes)

                )
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.EmailAddress },
                state = emailState,
                lineLimits = TextFieldLineLimits.SingleLine,
                label = { Text(stringResource(R.string.email)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            PasswordTextField(
                modifier = Modifier.fillMaxWidth(),
                state = passwordState
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onSignInWithEmail(
                        emailState.text.toString(),
                        passwordState.text.toString()
                    )
                }
            ) {
                Text(stringResource(R.string.sign_in))
            }

            // TODO : Add Sign up button / link
        }
    }

    if (isLoading) {
        FullScreenProgressIndicator()
    }
}

@Preview(showSystemUi = true, name = "Sign In Screen Light Mode")
@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Sign In Screen Dark Mode"
)
@Composable
fun SignInScreenContentPreview() {
    AisleronTheme {
        SignInScreenContent(
            onSignInWithEmail = { _, _ -> },
            syncServicePreference = SyncServicePreference.CUSTOM_SERVICE,
            syncServiceUrl = "https://aisleron.syncservice.com",
            snackbarHostState = null,
            isLoading = false
        )
    }
}