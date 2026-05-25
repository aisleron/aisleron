/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data.productvariant

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aisleron.data.base.SyncEntity
import com.aisleron.data.product.ProductEntity

@Entity(
    tableName = "ProductVariant",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["barcode"], unique = true),
        Index(value = ["productId"]),
        Index(value = ["syncId"], unique = true),
        Index(value = ["isRemoved", "id"]),
        Index(value = ["lastModifiedAt"])
    ]
)
data class ProductVariantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val productId: Int,
    val barcode: String,
    @ColumnInfo(defaultValue = "0") val createdAt: Long,
    override val syncId: String? = null,
    @ColumnInfo(defaultValue = "0") override val isRemoved: Boolean = false,
    @ColumnInfo(defaultValue = "0") override val lastModifiedAt: Long = System.currentTimeMillis(),
    override val serverUpdatedAt: Long? = null
): SyncEntity
