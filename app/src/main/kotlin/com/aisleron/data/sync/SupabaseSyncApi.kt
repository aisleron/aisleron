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

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class SupabaseSyncApi<Dto : SyncDto>(
    private val clientProvider: SupabaseClientProvider,
    private val serializer: KSerializer<Dto>,
    private val json: Json = Json { ignoreUnknownKeys = true },
    override val entityName: String,
    private val pushFunctionName: String = "push_$entityName",
    private val pullFunctionName: String = "pull_$entityName"
) : SyncApi<Dto> {
    override suspend fun push(dto: List<Dto>) {
        if (dto.isEmpty()) return

        val client = clientProvider.getClientOrNull()
            ?: throw IllegalStateException("Supabase client unavailable (unauthenticated or offline)")

        val jsonRecords = Json.encodeToJsonElement(
            ListSerializer(serializer),
            dto
        )

        client.postgrest.rpc(
            function = pushFunctionName,
            parameters = mapOf("p_records" to jsonRecords)
        )
    }

    override suspend fun fetchSince(lastUpdatedDateIso: String): List<Dto> {
        val client = clientProvider.getClientOrNull()
            ?: throw IllegalStateException("Supabase client unavailable (unauthenticated or offline)")

        val pSinceTimestamp =
            lastUpdatedDateIso.ifBlank { Instant.fromEpochMilliseconds(-1).toString() }

        val response = client.postgrest.rpc(
            function = pullFunctionName,
            parameters = mapOf("p_since_timestamp" to pSinceTimestamp)
        )

        return json.decodeFromString(
            deserializer = ListSerializer(serializer),
            string = response.data
        )
    }
}
