/*
 * Copyright (C) 2025 aisleron.com
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

package com.aisleron.data.product

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.aisleron.data.note.NoteEntity
import com.aisleron.domain.product.TrackingMode

@Entity(
    tableName = "Product",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val inStock: Boolean,
    @ColumnInfo(defaultValue = "0") val qtyNeeded: Double,
    @ColumnInfo(index = true) val noteId: Int?,
    @ColumnInfo(defaultValue = "1") val qtyIncrement: Double,
    @ColumnInfo(defaultValue = "") val unitOfMeasure: String,
    val trackingMode: TrackingMode?
)