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

import com.aisleron.domain.preferences.SyncPreferences
import io.github.jan.supabase.SupabaseClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SupabaseSessionManagerImplTest {
    private val syncPreferences: SyncPreferences = mockk()
    private val clientFactory: SupabaseClientFactory = mockk()
    private val authDelegate: SupabaseAuthDelegate = mockk()
    private val mockSupabaseClient: SupabaseClient = mockk(relaxed = true)

    private lateinit var sessionManager: SupabaseSessionManagerImpl

    @Before
    fun setUp() {
        sessionManager = SupabaseSessionManagerImpl(syncPreferences, clientFactory, authDelegate)
    }

    private fun initPreferences(backendUrl: String, backendKey: String) {
        every { syncPreferences.getBackendUrl() } returns backendUrl
        every { syncPreferences.getBackendKey() } returns backendKey
    }

    private fun initMocks(backendUrl: String, backendKey: String) {
        initPreferences(backendUrl, backendKey)
        every {
            clientFactory.create(backendUrl, backendKey)
        } returns mockSupabaseClient
    }

    @Test
    fun getClientOrNull_ValidCredentialsForNewClient_ReturnsClient() {
        val backendUrl = "https://example.supabase.co"
        val backendKey = "some-valid-key"
        initMocks(backendUrl, backendKey)

        val client = sessionManager.getClientOrNull()

        assertNotNull(client)
        assertEquals(mockSupabaseClient, client)
        verify(exactly = 1) { clientFactory.create(backendUrl, backendKey) }
    }

    @Test
    fun getClientOrNull_BlankCredentials_ReturnsNull() {
        val backendUrl = ""
        val backendKey = ""
        initPreferences(backendUrl, backendKey)

        val client = sessionManager.getClientOrNull()

        assertNull(client)
        verify(exactly = 0) { clientFactory.create(backendUrl, backendKey) }
    }

    @Test
    fun getClientOrNull_ErrorWithClient_ReturnsNull() {
        val backendUrl = "https://example.supabase.co"
        val backendKey = "some-valid-key"
        initPreferences(backendUrl, backendKey)
        every {
            clientFactory.create(backendUrl, backendKey)
        } throws RuntimeException("Network Error")

        val client = sessionManager.getClientOrNull()

        assertNull(client)
        verify(exactly = 1) { clientFactory.create(backendUrl, backendKey) }
    }

    @Test
    fun getClientOrNull_ClientExists_ReturnsExistingClient() {
        val backendUrl = "https://example.supabase.co"
        val backendKey = "some-valid-key"
        initPreferences(backendUrl, backendKey)
        every {
            clientFactory.create(backendUrl, backendKey)
        } returns mockk<SupabaseClient>(relaxed = true)

        val client1 = sessionManager.getClientOrNull()
        val client2 = sessionManager.getClientOrNull()

        assertEquals(client1, client2)
        verify(exactly = 1) { clientFactory.create(backendUrl, backendKey) }
    }

    @Test
    fun signInWithEmail_SuccessfulAuth_ReturnsSuccess() = runTest {
        val backendUrl = "https://example.supabase.co"
        val backendKey = "some-valid-key"
        initMocks(backendUrl, backendKey)
        coEvery {
            authDelegate.signInWithEmail(
                mockSupabaseClient, "test@example.com", "password123"
            )
        } returns Unit

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            authDelegate.signInWithEmail(
                mockSupabaseClient, "test@example.com", "password123"
            )
        }
    }

    @Test
    fun signInWithEmail_AuthThrowsException_ReturnsFailureAndClosesClient() = runTest {
        val backendUrl = "https://example.supabase.co"
        val backendKey = "some-valid-key"
        initMocks(backendUrl, backendKey)
        coEvery {
            authDelegate.signInWithEmail(
                mockSupabaseClient, "test@example.com", "password123"
            )
        } throws RuntimeException("Network Error")

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isFailure)
        coVerify(exactly = 1) {
            authDelegate.signInWithEmail(
                mockSupabaseClient, "test@example.com", "password123"
            )
        }

        coVerify(exactly = 1) { mockSupabaseClient.close() }
    }

    @Test
    fun signInWithEmail_ClientAcquisitionError_ReturnsFailure() = runTest {
        val backendUrl = "https://example.supabase.co"
        val backendKey = "some-valid-key"
        initPreferences(backendUrl, backendKey)
        every {
            clientFactory.create(backendUrl, backendKey)
        } throws RuntimeException("Network Error")

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isFailure)
    }

    @Test
    fun signOut_SuccessfulSignOut_ReturnsSuccess() = runTest {
        val backendUrl = "https://example.supabase.co"
        val backendKey = "some-valid-key"
        initMocks(backendUrl, backendKey)

        coEvery {
            authDelegate.signOut(mockSupabaseClient)
        } returns Unit

        val result = sessionManager.signOut()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authDelegate.signOut(mockSupabaseClient) }
        coVerify(exactly = 1) { mockSupabaseClient.close() }
    }

    @Test
    fun signOut_ClientIsNull_SignOutNotCalled() = runTest {
        val backendUrl = ""
        val backendKey = ""
        initMocks(backendUrl, backendKey)

        val result = sessionManager.signOut()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { authDelegate.signOut(mockSupabaseClient) }
    }
}