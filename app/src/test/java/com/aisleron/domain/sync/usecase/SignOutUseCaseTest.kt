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

package com.aisleron.domain.sync.usecase

import com.aisleron.domain.sync.SyncSessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SignOutUseCaseTest {
    private val sessionManager: SyncSessionManager = mockk()
    private lateinit var signOutUseCase: SignOutUseCase

    @BeforeEach
    fun setUp() {
        signOutUseCase = SignOutUseCase(sessionManager)
    }

    @Test
    fun invoke_IsInvalidLogout_ReturnError() = runTest {
        val errorMessage = "Logout Error"
        coEvery { sessionManager.signOut() } returns Result.failure(Exception(errorMessage))

        val result = signOutUseCase()

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)

        coVerify(exactly = 1) { sessionManager.signOut() }
    }

    @Test
    fun invoke_IsValidLogout_ReturnSuccess() = runTest {
        coEvery { sessionManager.signOut() } returns Result.success(Unit)

        val result = signOutUseCase()

        assertTrue(result.isSuccess)
        assertNull(result.exceptionOrNull())

        coVerify(exactly = 1) { sessionManager.signOut() }
    }
}