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

package com.aisleron.data.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType

@Entity(tableName = "Location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val type: LocationType,
    val defaultFilter: FilterType,
    val name: String,
    val pinned: Boolean,
    @ColumnInfo(defaultValue = "1") val showDefaultAisle: Boolean
)
