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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import kotlin.io.encoding.Base64
import kotlin.time.Clock

class AppMetadataTest {

    private fun createMockJwt(payloadJson: String): String {
        val header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}"

        val encoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)

        val encodedHeader = encoder.encode(header.encodeToByteArray())
        val encodedPayload = encoder.encode(payloadJson.encodeToByteArray())

        val dummySignature = "sig"

        return "$encodedHeader.$encodedPayload.$dummySignature"
    }

    /**
     * A token completely missing the app_metadata key.
     */
    private val tokenNoAppMetadata: String
        get() = createMockJwt("{\"sub\":\"123-456\"}")

    /**
     * A token where app_metadata has no subscription fields.
     * This happens for self-hosted sync services.
     */
    private val tokenNoSubscriptionMetadata: String
        get() = createMockJwt("{\"sub\":\"123-456\",\"app_metadata\":{\"provider\":\"email\"}}")

    /**
     * A token where the user is actively subscribed with an expiry date.
     */
    private val tokenIsSubscribed: String
        get() = createMockJwt("{\"sub\":\"123-456\",\"app_metadata\":{\"is_subscribed\":true,\"sub_expiry\":\"2099-11-09T08:41:56.043453+00:00\"}}")

    /**
     * A token where the user is explicitly not subscribed with an expiry date.
     */
    private val tokenNotSubscribed: String
        get() = createMockJwt("{\"sub\":\"123-456\",\"app_metadata\":{\"is_subscribed\":false,\"sub_expiry\":\"2025-11-09T08:41:56.043453+00:00\"}}")

    /**
     * A token where app_metadata exists, is_subscribed is false, and sub_expiry is null.
     * This happens where a user has no subscription record.
     */
    private val tokenNoSubscription: String
        get() = createMockJwt("{\"sub\":\"123-456\",\"app_metadata\":{\"is_subscribed\":false,\"sub_expiry\":null}}")

    @Test
    fun fromAccessToken_EmptyToken_SubscribedIsFalse() {
        val appMetadata = AppMetadata.fromAccessToken("")

        assertFalse(appMetadata.isSubscribed)
        assertNull(appMetadata.subscriptionExpiry)
    }

    @Test
    fun fromAccessToken_NoSubscriptionMetadata_SubscribedIsTrue() {
        val appMetadata = AppMetadata.fromAccessToken(tokenNoSubscriptionMetadata)

        assertTrue(appMetadata.isSubscribed)
        assertNull(appMetadata.subscriptionExpiry)
    }

    @Test
    fun fromAccessToken_IsSubscribed_SubscribedIsTrue() {
        val appMetadata = AppMetadata.fromAccessToken(tokenIsSubscribed)

        assertTrue(appMetadata.isSubscribed)
        assertTrue(appMetadata.subscriptionExpiry!! > Clock.System.now())
    }

    @Test
    fun fromAccessToken_NotSubscribed_SubscribedIsFalse() {
        val appMetadata = AppMetadata.fromAccessToken(tokenNotSubscribed)

        assertFalse(appMetadata.isSubscribed)
        assertTrue(appMetadata.subscriptionExpiry!! < Clock.System.now())
    }

    @Test
    fun fromAccessToken_NoSubscription_SubscribedIsFalse() {
        val appMetadata = AppMetadata.fromAccessToken(tokenNoSubscription)

        assertFalse(appMetadata.isSubscribed)
        assertNull(appMetadata.subscriptionExpiry)
    }

    @Test
    fun fromAccessToken_NoAppMetadata_SubscribedIsFalse() {
        val appMetadata = AppMetadata.fromAccessToken(tokenNoAppMetadata)

        assertFalse(appMetadata.isSubscribed)
        assertNull(appMetadata.subscriptionExpiry)
    }

    @Test
    fun fromAccessToken_InvalidToken_SubscribedIsFalse() {
        val appMetadata = AppMetadata.fromAccessToken("abc.123.xyz")

        assertFalse(appMetadata.isSubscribed)
        assertNull(appMetadata.subscriptionExpiry)
    }
}