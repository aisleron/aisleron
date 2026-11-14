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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import kotlin.coroutines.cancellation.CancellationException

class SignInWithGithubUseCaseTest {
    private lateinit var sessionManager: SyncSessionManagerTestImpl
    private lateinit var signInWithGithubUseCase: SignInWithGithubUseCase

    @BeforeEach
    fun setUp() {
        sessionManager = SyncSessionManagerTestImpl(SyncSessionStatus.NotConfigured)
        signInWithGithubUseCase = SignInWithGithubUseCase(sessionManager)
    }

    @Test
    fun invoke_IsInvalidLogin_ReturnError() = runTest {
        val errorMessage = "Login Error"

        sessionManager.failWith(
            AisleronException.AuthException(
                AisleronException.ExceptionCode.AUTH_EXCEPTION,
                errorMessage
            )
        )

        val result = signInWithGithubUseCase()

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }

    @Test
    fun invoke_GenericError_ReturnError() = runTest {
        sessionManager.failWith(Exception("Error"))

        val result = signInWithGithubUseCase()

        assertTrue(result.isFailure)

        val resultException = result.exceptionOrNull()
        assertInstanceOf<AisleronException.SignInException>(resultException)
    }

    @Test
    fun invoke_IsValidLogin_ReturnSuccess() = runTest {
        val result = signInWithGithubUseCase()

        assertTrue(result.isSuccess)
        assertNull(result.exceptionOrNull())
        assertEquals(
            SyncSessionManagerTestImpl.SignedInProvider.GITHUB,
            sessionManager.signedInProvider
        )
    }

    @Test
    fun invoke_IsCancellationException_ThrowsCancellationException() = runTest {
        sessionManager.failWith(CancellationException())

        assertThrows<CancellationException> {
            signInWithGithubUseCase()
        }
    }

}