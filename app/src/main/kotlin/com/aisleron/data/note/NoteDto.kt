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

package com.aisleron.data.note

import com.aisleron.data.sync.SyncDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    @SerialName("id") override val id: String,
    @SerialName("is_deleted") override val isDeleted: Boolean,
    @SerialName("client_updated_at") override val clientUpdatedAt: String,
    @SerialName("server_updated_at") override val serverUpdatedAt: String? = null,
    @SerialName("note_text") val noteText: String,
    @SerialName("created_at") val createdAt: String
) : SyncDto