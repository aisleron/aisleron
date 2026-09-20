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

class SupabaseClientFactoryTestImpl : SupabaseClientFactory {
    private lateinit var _client: SupabaseClient

    private var _createCallCount: Int = 0
    val createCallCount: Int get() = _createCallCount

    private var _failWithException: Throwable? = null

    override fun create(url: String, key: String): SupabaseClient {
        _createCallCount += 1
        _failWithException?.let { throw it }

        return _client
    }

    fun setClient(client: SupabaseClient) {
        _client = client
    }

    fun failWith(throwable: Throwable?) {
        _failWithException = throwable
    }
}