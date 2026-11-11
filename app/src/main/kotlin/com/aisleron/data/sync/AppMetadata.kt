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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.time.Instant

@Serializable
data class AppMetadata(
    @SerialName("is_subscribed")
    val isSubscribed: Boolean = true,

    @SerialName("sub_expiry")
    val subscriptionExpiry: Instant? = null
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun fromAccessToken(accessToken: String): AppMetadata {
            return runCatching {
                val parts = accessToken.split(".")
                if (parts.size < 2) return AppMetadata(isSubscribed = false)

                val bytes =
                    Base64.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(parts[1])

                val decoded = json.decodeFromString<JwtPayload>(bytes.decodeToString())
                decoded.appMetadata ?: AppMetadata(isSubscribed = false)
            }.onFailure { exception ->
                exception.printStackTrace()
            }.getOrDefault(AppMetadata(isSubscribed = false))
        }
    }
}

@Serializable
private data class JwtPayload(
    @SerialName("app_metadata")
    val appMetadata: AppMetadata? = null
)