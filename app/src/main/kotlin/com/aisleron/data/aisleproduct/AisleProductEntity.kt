/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.data.aisleproduct

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aisleron.data.base.SyncEntity

@Entity(
    tableName = "AisleProduct",
    indices = [
        Index(value = ["aisleId", "productId"], unique = true),
        Index(value = ["syncId"], unique = true),
        Index(value = ["isRemoved", "id"]),
        Index(value = ["lastModifiedAt"])
    ]
)
data class AisleProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val aisleId: Int,
    val productId: Int,
    val rank: Int,
    override val syncId: String? = null,
    @ColumnInfo(defaultValue = "0") override val isRemoved: Boolean = false,
    @ColumnInfo(defaultValue = "0") override val lastModifiedAt: Long = System.currentTimeMillis(),
    override val serverUpdatedAt: Long? = null
) : SyncEntity