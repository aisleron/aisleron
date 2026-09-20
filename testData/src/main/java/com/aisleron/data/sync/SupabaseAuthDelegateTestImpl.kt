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

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SupabaseAuthDelegateTestImpl : SupabaseAuthDelegate {
    private var _sessionStatus: SessionStatus = SessionStatus.Initializing
    private var _failWithException: Throwable? = null

    private var _awaitInitializationDelay: Duration = 0.milliseconds

    private var _signInWithEmailCallCount: Int = 0
    val signInWithEmailCallCount: Int get() = _signInWithEmailCallCount

    private var _getSessionStatusFlowCallCount: Int = 0
    val getSessionStatusFlowCallCount: Int get() = _getSessionStatusFlowCallCount

    private var _signOutCallCount: Int = 0
    val signOutCallCount: Int get() = _signOutCallCount

    override suspend fun signInWithEmail(
        client: SupabaseClient, email: String, password: String
    ) {
        _signInWithEmailCallCount += 1
        _failWithException?.let { throw it }
    }

    override suspend fun signOut(client: SupabaseClient) {
        _signOutCallCount += 1
        _failWithException?.let { throw it }
    }

    override fun getSessionStatusFlow(client: SupabaseClient): Flow<SessionStatus> {
        _getSessionStatusFlowCallCount += 1
        _failWithException?.let { throw it }

        return flowOf(_sessionStatus)
    }

    override suspend fun awaitInitialization(client: SupabaseClient) {
        _failWithException?.let { throw it }

        delay(_awaitInitializationDelay)
    }

    fun failWith(throwable: Throwable?) {
        _failWithException = throwable
    }

    fun setAwaitInitializationDelay(delay: Duration) {
        _awaitInitializationDelay = delay
    }

    fun setSessionStatus(supabaseSessionStatus: SessionStatus) {
        _sessionStatus = supabaseSessionStatus
    }
}