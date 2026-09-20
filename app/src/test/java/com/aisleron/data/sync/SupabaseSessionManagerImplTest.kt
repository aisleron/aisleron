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

package com.aisleron.data.sync

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.base.exceptionCode
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.testdata.data.log.LoggerTestImpl
import com.aisleron.testdata.data.preferences.syncpreferences.SyncPreferencesRepositoryTestImpl
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SupabaseSessionManagerImplTest {
    private lateinit var syncPreferencesRepository: SyncPreferencesRepositoryTestImpl
    private lateinit var clientFactory: SupabaseClientFactoryTestImpl
    private lateinit var authDelegate: SupabaseAuthDelegateTestImpl
    private val mockSupabaseClient: SupabaseClient = mockk()
    private lateinit var sessionManager: SupabaseSessionManagerImpl
    private lateinit var logger: LoggerTestImpl

    @BeforeEach
    fun setUp() {
        syncPreferencesRepository = SyncPreferencesRepositoryTestImpl()
        syncPreferencesRepository.resetSyncPreferences()
        logger = LoggerTestImpl()
        authDelegate = SupabaseAuthDelegateTestImpl()
        clientFactory = SupabaseClientFactoryTestImpl()
        sessionManager =
            SupabaseSessionManagerImpl(
                syncPreferencesRepository, clientFactory, authDelegate, logger
            )
    }

    private fun initPreferences(serviceUrl: String, serviceKey: String) {
        val syncPreferences = syncPreferencesRepository.getDefaultSyncPreferences().copy(
            serviceUrl = serviceUrl,
            serviceKey = serviceKey
        )

        syncPreferencesRepository.setSyncPreferences(syncPreferences)
    }

    private fun initMocks(serviceUrl: String, serviceKey: String) {
        initPreferences(serviceUrl, serviceKey)
        clientFactory.setClient(mockSupabaseClient)
    }

    @Test
    fun getClientOrNull_ValidCredentialsForNewClient_ReturnsClient() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initMocks(serviceUrl, serviceKey)

        val client = sessionManager.getClientOrNull()

        assertNotNull(client)
        assertEquals(mockSupabaseClient, client)
        assertEquals(1, clientFactory.createCallCount)
    }

    @Test
    fun getClientOrNull_BlankUrl_ReturnsNull() = runTest {
        val serviceUrl = ""
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)

        val client = sessionManager.getClientOrNull()

        assertNull(client)
        assertEquals(0, clientFactory.createCallCount)
    }

    @Test
    fun getClientOrNull_BlankKey_ReturnsNull() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = ""
        initPreferences(serviceUrl, serviceKey)

        val client = sessionManager.getClientOrNull()

        assertNull(client)
        assertEquals(0, clientFactory.createCallCount)
    }

    @Test
    fun getClientOrNull_ErrorWithClient_ReturnsNull() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)
        clientFactory.failWith(RuntimeException("Network Error"))

        val client = sessionManager.getClientOrNull()

        assertNull(client)

        assertEquals(1, clientFactory.createCallCount)
    }

    @Test
    fun getClientOrNull_ErrorClosingClient_LogsError() = runTest {
        val url1 = "https://one.supabase.co"
        val key1 = "some-valid-key-one"
        initPreferences(url1, key1)
        clientFactory.setClient(getMockClient(url1, key1, errorOnClose = true))

        sessionManager.getClientOrNull()

        val url2 = "https://two.supabase.co"
        val key2 = "some-valid-key-two"
        initPreferences(url2, key2)
        clientFactory.setClient(getMockClient(url2, key2, errorOnClose = true))

        val client2 = sessionManager.getClientOrNull()

        assertNotNull(client2)

        val logParameters = logger.getEParameters()
        assertEquals("SupabaseSessionManager", logParameters.tag)
        assertInstanceOf<RuntimeException>(logParameters.throwable)
    }

    private fun getMockClient(
        url: String, key: String, errorOnClose: Boolean = false
    ): SupabaseClient {
        val client = mockk<SupabaseClient> {
            every { supabaseHttpUrl } returns url
            every { supabaseKey } returns key
        }

        if (errorOnClose)
            coEvery { client.close() } throws RuntimeException("Network Error")
        else
            coEvery { client.close() } returns Unit
        return client
    }

    @Test
    fun getClientOrNull_ClientExists_ReturnsExistingClient() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)
        clientFactory.setClient(getMockClient(serviceUrl, serviceKey))

        val client1 = sessionManager.getClientOrNull()
        val client2 = sessionManager.getClientOrNull()

        assertEquals(client1, client2)

        assertEquals(1, clientFactory.createCallCount)
    }

    @Test
    fun getClientOrNull_HasDifferentAttributes_ReturnsNewClient() = runTest {
        val url1 = "https://one.supabase.co"
        val key1 = "some-valid-key-one"
        initPreferences(url1, key1)
        clientFactory.setClient(getMockClient(url1, key1))

        val client1 = sessionManager.getClientOrNull()

        val url2 = "https://two.supabase.co"
        val key2 = "some-valid-key-two"
        initPreferences(url2, key2)
        clientFactory.setClient(getMockClient(url2, key2))

        val client2 = sessionManager.getClientOrNull()

        assertNotEquals(client1, client2)
        coVerify(exactly = 1) { client1?.close() }
    }

    @Test
    fun signInWithEmail_SuccessfulAuth_ReturnsSuccess() = runTest {
        initMocks("https://example.supabase.co", "some-valid-key")

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(1, authDelegate.signInWithEmailCallCount)
    }

    @Test
    fun signInWithEmail_UnmappedException_ResultHasSignInException() = runTest {
        initMocks("https://example.supabase.co", "some-valid-key")
        authDelegate.failWith(RuntimeException("Network Error"))

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isFailure)
        assertInstanceOf<AisleronException.SignInException>(result.exceptionOrNull())
    }

    private suspend fun <T : AisleronException> signInWithEmail_ValidateAisleronExceptions(
        throwable: Throwable,
        expectedExceptionClass: KClass<T>,
        expectedExceptionCode: AisleronException.ExceptionCode
    ) {
        initMocks("https://example.supabase.co", "some-valid-key")
        authDelegate.failWith(throwable)

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isFailure)

        val resultException = result.exceptionOrNull()
        assertTrue(expectedExceptionClass.isInstance(resultException))
        assertEquals(expectedExceptionCode, resultException.exceptionCode)
    }

    @Test
    fun signInWithEmail_InvalidCredentials_ResultHasInvalidCredentialsException() = runTest {
        signInWithEmail_ValidateAisleronExceptions(
            throwable = AuthRestException(
                AuthErrorCode.InvalidCredentials.value,
                "Error",
                mockk<HttpResponse>(relaxed = true)
            ),
            expectedExceptionClass = AisleronException.AuthException::class,
            expectedExceptionCode = AisleronException.ExceptionCode.INVALID_CREDENTIAL_EXCEPTION
        )
    }

    @Test
    fun signInWithEmail_EmailNotConfigured_ResultHasUnconfirmedEmailException() = runTest {
        signInWithEmail_ValidateAisleronExceptions(
            throwable = AuthRestException(
                AuthErrorCode.EmailNotConfirmed.value,
                "Error",
                mockk<HttpResponse>(relaxed = true)
            ),
            expectedExceptionClass = AisleronException.AuthException::class,
            expectedExceptionCode = AisleronException.ExceptionCode.UNCONFIRMED_EMAIL_EXCEPTION
        )
    }

    @Test
    fun signInWithEmail_UnknownAuthException_ResultHasAuthException() = runTest {
        signInWithEmail_ValidateAisleronExceptions(
            throwable = AuthRestException(
                AuthErrorCode.UserSsoManaged.value,
                "Error",
                mockk<HttpResponse>(relaxed = true)
            ),
            expectedExceptionClass = AisleronException.AuthException::class,
            expectedExceptionCode = AisleronException.ExceptionCode.AUTH_EXCEPTION
        )
    }

    @Test
    fun signInWithEmail_HttpRequestException_ResultHasNetworkException() = runTest {
        signInWithEmail_ValidateAisleronExceptions(
            throwable = HttpRequestException(
                "Error",
                HttpRequestBuilder()
            ),
            expectedExceptionClass = AisleronException.NetworkException::class,
            expectedExceptionCode = AisleronException.ExceptionCode.NETWORK_EXCEPTION
        )
    }

    @Test
    fun signInWithEmail_AisleronException_ResultHasAisleronException() = runTest {
        signInWithEmail_ValidateAisleronExceptions(
            throwable = AisleronException.SampleDataCreationException(),
            expectedExceptionClass = AisleronException.SampleDataCreationException::class,
            expectedExceptionCode = AisleronException.ExceptionCode.SAMPLE_DATA_CREATION_EXCEPTION
        )
    }

    @Test
    fun signInWithEmail_ClientAcquisitionError_ReturnsFailure() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)
        clientFactory.failWith(RuntimeException("Network Error"))

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isFailure)
    }

    @Test
    fun signOut_SuccessfulSignOut_ReturnsSuccess() = runTest {
        initMocks("https://example.supabase.co", "some-valid-key")

        val result = sessionManager.signOut()

        assertTrue(result.isSuccess)
        assertEquals(1, authDelegate.signOutCallCount)
    }

    @Test
    fun signOut_ClientIsNull_SignOutNotCalled() = runTest {
        val serviceUrl = ""
        val serviceKey = ""
        initMocks(serviceUrl, serviceKey)

        val result = sessionManager.signOut()

        assertTrue(result.isSuccess)
        assertEquals(0, authDelegate.signOutCallCount)
    }

    @Test
    fun signOut_ThrowsException_ReturnsFailure() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        val exceptionMessage = "Sign out error"
        initMocks(serviceUrl, serviceKey)
        authDelegate.failWith(Exception(exceptionMessage))

        val result = sessionManager.signOut()

        assertTrue(result.isFailure)

        val resultException = result.exceptionOrNull()
        assertInstanceOf<AisleronException.SignOutException>(resultException)
        assertEquals(exceptionMessage, resultException.cause?.message)
    }

    private suspend fun validateStatus_ArrangeActAssert(
        supabaseSessionStatus: SessionStatus, syncSessionStatus: SyncSessionStatus
    ) {
        initMocks("https://example.supabase.co", "some-valid-key")
        authDelegate.setSessionStatus(supabaseSessionStatus)

        sessionManager.refreshStatus()

        val resultSessionStatus: SyncSessionStatus = sessionManager.sessionStatus.first()
        assertEquals(syncSessionStatus, resultSessionStatus)
        assertEquals(1, authDelegate.getSessionStatusFlowCallCount)
    }

    @Test
    fun sessionStatus_AuthStatusIsAuthenticated_SyncSessionStatusIsAuthenticated() = runTest {
        val userEmail = "a@b.c"

        val userInfo = UserInfo(
            aud = "",
            email = userEmail,
            id = ""
        )

        val userSession = UserSession(
            accessToken = "",
            refreshToken = "",
            expiresIn = 0,
            tokenType = "",
            user = userInfo
        )

        validateStatus_ArrangeActAssert(
            supabaseSessionStatus = SessionStatus.Authenticated(userSession),
            syncSessionStatus = SyncSessionStatus.Authenticated(userEmail)
        )
    }

    @Test
    fun sessionStatus_AuthStatusIsNotAuthenticated_SyncSessionStatusIsNotAuthenticated() = runTest {
        validateStatus_ArrangeActAssert(
            supabaseSessionStatus = SessionStatus.NotAuthenticated(),
            syncSessionStatus = SyncSessionStatus.NotAuthenticated
        )
    }

    @Test
    fun sessionStatus_AuthStatusIsInitializing_SyncSessionStatusIsLoading() = runTest {
        validateStatus_ArrangeActAssert(
            supabaseSessionStatus = SessionStatus.Initializing,
            syncSessionStatus = SyncSessionStatus.Loading
        )
    }

    @Test
    fun sessionStatus_AuthStatusIsRefreshFailure_SyncSessionStatusIsRefreshFailure() = runTest {
        val cause = RefreshFailureCause.NetworkError(Exception())

        validateStatus_ArrangeActAssert(
            supabaseSessionStatus = SessionStatus.RefreshFailure(cause),
            syncSessionStatus = SyncSessionStatus.RefreshFailure
        )
    }

    @Test
    fun sessionStatus_ClientIsNull_SyncSessionStatusIsNotConfigured() = runTest {
        initMocks("", "")

        sessionManager.refreshStatus()

        val resultSessionStatus: SyncSessionStatus = sessionManager.sessionStatus.first()
        assertEquals(SyncSessionStatus.NotConfigured, resultSessionStatus)
    }

    @Test
    fun getConnectedClientOrNull_ClientConnected_ReturnsClient() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initMocks(serviceUrl, serviceKey)

        val client = sessionManager.getConnectedClientOrNull()

        assertNotNull(client)
        assertEquals(mockSupabaseClient, client)
    }

    @Test
    fun getConnectedClientOrNull_WaitTimesOut_ReturnsClient() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        val waitTime = 1.seconds

        initMocks(serviceUrl, serviceKey)
        authDelegate.setAwaitInitializationDelay(waitTime)

        val client = sessionManager.getConnectedClientOrNull(500.milliseconds)

        assertNotNull(client)
        assertNotEquals("", logger.getWParameters().message)
    }

    @Test
    fun getConnectedClientOrNull_clientIsNull_ReturnsNull() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)
        clientFactory.failWith(RuntimeException("Network Error"))

        val client = sessionManager.getConnectedClientOrNull()

        assertNull(client)
    }
}