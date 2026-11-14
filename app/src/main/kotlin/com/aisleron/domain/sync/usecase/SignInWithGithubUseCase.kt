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
import com.aisleron.domain.base.extension.recoverCatchingWithAisleronException
import com.aisleron.domain.sync.SyncSessionManager

class SignInWithGithubUseCase(private val sessionManager: SyncSessionManager) {
    suspend operator fun invoke(): Result<Unit> {
        return sessionManager.signInWithGithub()
            .recoverCatchingWithAisleronException { cause ->
                AisleronException.SignInException(
                    message = "An error occurred while signing in",
                    cause = cause
                )
            }
    }
}