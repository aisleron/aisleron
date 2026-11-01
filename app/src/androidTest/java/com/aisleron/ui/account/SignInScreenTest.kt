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

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import com.aisleron.R
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.testdata.data.sync.SyncSessionManagerTestImpl
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.ComposeScreenTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SignInScreenTest : ComposeScreenTest() {
    override val koinModules = listOf(
        useCaseModule, preferenceTestModule, generalTestModule, viewModelTestModule
    )

    private fun ComposeUiTest.performLogin(
        email: String,
        password: String,
        onSignInSuccess: () -> Unit = {}
    ) {
        val passwordLabel = getString(R.string.password)
        val signInButtonText = getString(R.string.sign_in)
        val emailLabel = getString(R.string.email)

        setContent {
            SignInScreen(onSignInSuccess = onSignInSuccess)
        }

        onNodeWithText(emailLabel).performTextReplacement(email)
        onNodeWithText(passwordLabel).performTextReplacement(password)
        onNodeWithText(signInButtonText).performClick()
        waitForIdle()
    }

    @Test
    fun signIn_OnSignInSuccess_InvokeOnSignInSuccess() = runKoinComposeUiTest {
        var onSuccessCalled = false
        val onSignInSuccess = { onSuccessCalled = true }
        performLogin("user@example.com", "SecurePassword123", onSignInSuccess)

        assertTrue(onSuccessCalled)
    }

    @Test
    fun signIn_OnSignInFailure_DisplaysErrorSnackbar() = runKoinComposeUiTest {
        val exceptionCode = AisleronException.ExceptionCode.INVALID_CREDENTIAL_EXCEPTION
        val sessionManager = get<SyncSessionManager>() as SyncSessionManagerTestImpl
        sessionManager.failWith(AisleronException.AuthException(exceptionCode))
        val expectedErrorText = getString(
            AisleronExceptionMap().getErrorResourceId(exceptionCode)
        )

        performLogin("invalid@example.com", "WrongPassword")

        onNodeWithText(expectedErrorText).assertIsDisplayed()
    }

    @Test
    fun signInContent_InitialState_DisplaysConnectingUrlAndFields() = runComposeUiTest {
        val testUrl = "https://sync.aisleron.com"
        val connectingToText = getString(R.string.sync_service_connecting_to)
        val emailLabel = getString(R.string.email)
        val signInButtonText = getString(R.string.sign_in)

        setContent {
            SignInScreenContent(
                onSignInWithEmail = { _, _ -> },
                syncServicePreference = SyncServicePreference.CUSTOM_SERVICE,
                syncServiceUrl = testUrl,
                snackbarHostState = null,
                isLoading = false
            )
        }

        onNodeWithText(connectingToText).assertIsDisplayed()
        onNodeWithText(testUrl).assertIsDisplayed()
        onNodeWithText(emailLabel).assertIsDisplayed()
        onNodeWithText(signInButtonText).assertIsDisplayed()
    }

    @Test
    fun signInContent_LoadingState_DisplaysProgressIndicator() = runComposeUiTest {
        setContent {
            SignInScreenContent(
                onSignInWithEmail = { _, _ -> },
                syncServicePreference = SyncServicePreference.CUSTOM_SERVICE,
                syncServiceUrl = "https://sync.aisleron.com",
                snackbarHostState = null,
                isLoading = true
            )
        }

        onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun signInContent_SubmitCredentials_InvokesSignInWithEmailCallback() = runComposeUiTest {
        val emailLabel = getString(R.string.email)
        val passwordLabel = getString(R.string.password)
        val signInButtonText = getString(R.string.sign_in)

        val inputEmail = "user@example.com"
        val inputPassword = "SecurePassword123"

        var actualEmail = ""
        var actualPassword = ""

        setContent {
            SignInScreenContent(
                onSignInWithEmail = { email, password ->
                    actualEmail = email
                    actualPassword = password
                },
                syncServicePreference = SyncServicePreference.CUSTOM_SERVICE,
                syncServiceUrl = "https://sync.aisleron.com",
                snackbarHostState = null,
                isLoading = false
            )
        }

        onNodeWithText(emailLabel).performTextReplacement(inputEmail)
        onNodeWithText(passwordLabel).performTextReplacement(inputPassword)

        onNodeWithText(signInButtonText).performClick()

        assertEquals(inputEmail, actualEmail)
        assertEquals(inputPassword, actualPassword)
    }

    @Test
    fun signInContent_IsDefaultSyncService_DisplaysDefaultSyncServiceText() = runComposeUiTest {
        val testUrl = "https://sync.aisleron.com"
        val connectingToText = getString(R.string.sync_service_connecting_to)
        val emailLabel = getString(R.string.email)
        val signInButtonText = getString(R.string.sign_in)
        val expectedConnectedTo = getString(R.string.sync_service_default)

        setContent {
            SignInScreenContent(
                onSignInWithEmail = { _, _ -> },
                syncServicePreference = SyncServicePreference.DEFAULT_SERVICE,
                syncServiceUrl = testUrl,
                snackbarHostState = null,
                isLoading = false
            )
        }

        onNodeWithText(connectingToText).assertIsDisplayed()
        onNodeWithText(expectedConnectedTo).assertIsDisplayed()
        onNodeWithText(emailLabel).assertIsDisplayed()
        onNodeWithText(signInButtonText).assertIsDisplayed()
    }
}