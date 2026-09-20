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
import com.aisleron.domain.base.extension.recoverCatchingUnlessCancelled
import com.aisleron.domain.base.extension.runCatchingUnlessCancelled
import com.aisleron.domain.log.Logger
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.domain.sync.SyncSessionStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class SupabaseSessionManagerImpl(
    private val syncPreferencesRepository: SyncPreferencesRepository,
    private val clientFactory: SupabaseClientFactory,
    private val authDelegate: SupabaseAuthDelegate,
    private val logger: Logger
) : SyncSessionManager, SupabaseClientProvider {
    private var activeClient: SupabaseClient? = null
    private val clientRefreshTrigger = MutableStateFlow(System.currentTimeMillis())

    override val sessionStatus: Flow<SyncSessionStatus> = clientRefreshTrigger.flatMapLatest {
        getClientOrNull()?.let {
            authDelegate.getSessionStatusFlow(it).map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> SyncSessionStatus.Authenticated(status.session.user?.email.orEmpty())
                    is SessionStatus.NotAuthenticated -> SyncSessionStatus.NotAuthenticated
                    is SessionStatus.Initializing -> SyncSessionStatus.Loading
                    is SessionStatus.RefreshFailure -> SyncSessionStatus.RefreshFailure
                }
            }
        } ?: flowOf(SyncSessionStatus.NotConfigured)
    }

    override fun refreshStatus() {
        clientRefreshTrigger.value = System.currentTimeMillis()
    }

    override suspend fun getClientOrNull(): SupabaseClient? {
        val syncPreferences = syncPreferencesRepository.getSyncPreferences()
        val savedUrl = syncPreferences.serviceUrl
        val savedKey = syncPreferences.serviceKey

        if (
            activeClient != null
            && activeClient?.supabaseHttpUrl == savedUrl
            && activeClient?.supabaseKey == savedKey
        ) return activeClient

        activeClient?.let { client ->
            try {
                runCatchingUnlessCancelled {
                    client.close()
                }.onFailure { throwable ->
                    logger.e(
                        tag = TAG,
                        message = "Error closing old client configuration",
                        throwable = throwable
                    )
                }
            } finally {
                // Need try-finally to explicitly free the activeClient even on cancellationException
                activeClient = null
            }
        }

        if (savedUrl.isNotBlank() && savedKey.isNotBlank()) {
            runCatchingUnlessCancelled {
                logger.d(TAG, "Provisioning client on demand...")
                clientFactory.create(savedUrl, savedKey)
            }.onSuccess { newClient ->
                activeClient = newClient
            }.onFailure { throwable ->
                logger.e(TAG, "Failed to build Supabase client", throwable)
            }
        }

        return activeClient
    }

    override suspend fun getConnectedClientOrNull(timeout: Duration): SupabaseClient? {
        val client = getClientOrNull() ?: return null

        logger.d(TAG, "Awaiting Auth initialization...")

        withTimeoutOrNull(timeout) {
            authDelegate.awaitInitialization(client)
            logger.d(TAG, "Auth initialization completed.")
        } ?: logger.w(
            TAG, "Auth initialization timed out after $timeout. Returning client as-is."
        )

        return client
    }

    private fun mapException(throwable: Throwable): Throwable {
        return when (throwable) {
            is AuthRestException -> {
                var message: String
                var exceptionCode: AisleronException.ExceptionCode

                when (throwable.errorCode) {
                    AuthErrorCode.InvalidCredentials -> {
                        message = "Invalid email or password"
                        exceptionCode =
                            AisleronException.ExceptionCode.INVALID_CREDENTIAL_EXCEPTION
                    }

                    AuthErrorCode.EmailNotConfirmed -> {
                        message = "Email address not confirmed"
                        exceptionCode =
                            AisleronException.ExceptionCode.UNCONFIRMED_EMAIL_EXCEPTION
                    }

                    else -> {
                        message = "Authentication failed"
                        exceptionCode = AisleronException.ExceptionCode.AUTH_EXCEPTION
                    }
                }

                AisleronException.AuthException(exceptionCode, message, cause = throwable)
            }

            is HttpRequestException -> {
                AisleronException.NetworkException(
                    exceptionCode = AisleronException.ExceptionCode.NETWORK_EXCEPTION,
                    message = "Unable to connect to sync service",
                    cause = throwable
                )
            }

            is AisleronException -> throwable

            else -> AisleronException.SignInException(
                message = "An unexpected error occurred while signing in",
                cause = throwable
            )
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return runCatchingUnlessCancelled {
            val client = getClientOrNull()
                ?: throw IllegalStateException("Failed to acquire client")

            authDelegate.signInWithEmail(client, email, password)
        }.also {
            refreshStatus()
        }.recoverCatchingUnlessCancelled { throwable ->
            throw mapException(throwable)
        }
    }

    override suspend fun signOut(): Result<Unit> =
        runCatchingUnlessCancelled {
            getClientOrNull()?.let { authDelegate.signOut(it) } ?: Unit
        }.also {
            refreshStatus()
        }.recoverCatchingUnlessCancelled { throwable ->
            throw AisleronException.SignOutException(
                message = "Error on signing out",
                cause = throwable
            )
        }

    companion object {
        const val TAG = "SupabaseSessionManager"
    }
}