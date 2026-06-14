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

import android.util.Log
import com.aisleron.domain.preferences.SyncPreferences
import com.aisleron.domain.sync.SyncSessionManager
import io.github.jan.supabase.SupabaseClient

class SupabaseSessionManagerImpl(
    private val syncPreferences: SyncPreferences,
    private val clientFactory: SupabaseClientFactory,
    private val authDelegate: SupabaseAuthDelegate
) : SyncSessionManager, SupabaseClientProvider {
    private var activeClient: SupabaseClient? = null

    override fun getClientOrNull(): SupabaseClient? {
        if (activeClient != null) return activeClient

        val savedUrl = syncPreferences.getBackendUrl()
        val savedKey = syncPreferences.getBackendKey()

        if (savedUrl.isNotBlank() && savedKey.isNotBlank()) {
            try {
                Log.d("SupabaseSessionManager", "Provisioning client on demand...")
                activeClient = clientFactory.create(savedUrl, savedKey)
            } catch (e: Exception) {
                Log.e("SupabaseSessionManager", "Failed to build Supabase client", e)
                activeClient = null
            }
        }

        return activeClient
    }

    private suspend fun closeActiveClient() {
        try {
            activeClient?.close()
        } catch (e: Exception) {
            Log.e("SupabaseSessionManager", "Error closing client", e)
        } finally {
            activeClient = null
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        runCatching {
            val client = getClientOrNull()
                ?: throw IllegalStateException("Failed to acquire client.")

            authDelegate.signInWithEmail(client, email, password)
        }.onFailure {
            closeActiveClient()
        }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            getClientOrNull()?.let { authDelegate.signOut(it) } ?: Unit
        }.also {
            closeActiveClient()
        }
}