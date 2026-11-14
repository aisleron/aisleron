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

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.testdata.data.sync.SyncSessionManagerTestImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows

class SignOutUseCaseTest {
    private lateinit var sessionManager: SyncSessionManagerTestImpl
    private lateinit var signOutUseCase: SignOutUseCase

    @BeforeEach
    fun setUp() {
        sessionManager = SyncSessionManagerTestImpl(SyncSessionStatus.NotConfigured)
        signOutUseCase = SignOutUseCase(sessionManager)
    }

    @Test
    fun invoke_IsInvalidLogout_ReturnsFailureResult() = runTest {
        val errorMessage = "Sign Out Error"
        sessionManager.failWith(Exception(errorMessage))

        val result = signOutUseCase()

        assertTrue(result.isFailure)
        val exception =
            assertInstanceOf<AisleronException.SignOutException>(result.exceptionOrNull())

        assertEquals(errorMessage, exception.cause?.message)
    }

    @Test
    fun invoke_IsValidLogout_ReturnsSuccessResult() = runTest {
        sessionManager.setSignedInProvider(SyncSessionManagerTestImpl.SignedInProvider.EMAIL)

        val result = signOutUseCase()

        assertTrue(result.isSuccess)
        assertNull(result.exceptionOrNull())
        assertEquals(
            SyncSessionManagerTestImpl.SignedInProvider.NONE,
            sessionManager.signedInProvider
        )
    }

    @Test
    fun invoke_IsCancellationException_ThrowsCancellationException() = runTest {
        sessionManager.failWith(CancellationException())

        assertThrows<CancellationException> {
            signOutUseCase()
        }
    }
}